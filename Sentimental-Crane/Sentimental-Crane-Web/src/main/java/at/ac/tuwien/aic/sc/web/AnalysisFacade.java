package at.ac.tuwien.aic.sc.web;

import at.ac.tuwien.aic.sc.core.AnalysisResult;
import at.ac.tuwien.aic.sc.core.entities.Company;
import at.ac.tuwien.aic.sc.core.event.AnalysisEndEvent;
import at.ac.tuwien.aic.sc.core.event.AnalysisStartEvent;
import at.ac.tuwien.aic.sc.core.event.ServerInstanceChangeEvent;
import org.apache.commons.lang3.time.DateUtils;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.apache.commons.lang3.time.DateUtils.addHours;

/**
 * @author Dominik Strasser, dominikstr@gmail.com
 */
@ApplicationScoped
@Stateless
public class AnalysisFacade {
	private static final Logger logger = Logger.getLogger(AnalysisFacade.class.getName());
	@Inject
	@EJB
	AnalysisScheduler analysisScheduler;

	@Inject
	Event<AnalysisStartEvent> startBus;

	@Inject
	Event<AnalysisEndEvent> endBus;

	private Integer serversOnline = 0;

	//@Asynchronous
	public Double analyse(Company company, Date from, Date to) {
		if (company == null)
			throw new IllegalArgumentException("Company hasn't to be null");

		//tell everybody who is interest, we are performing a new analysis
		AnalysisStartEvent e = new AnalysisStartEvent(company.getName(), from, to);
		startBus.fire(e);
		try {
			Thread.sleep(7000);
		} catch (InterruptedException ex) {
			logger.log(Level.WARNING, "Load balancing interrupted", ex);
		}

		//start analysis in background
		to=DateUtils.setHours(to,12);
		int hours = Math.max(24, (int) TimeUnit.MILLISECONDS.toHours(to.getTime() - from.getTime())) / 8;
		List<Future<AnalysisResult>> futures = new ArrayList<Future<AnalysisResult>>();
		for (int i = 0; i < 8; i++) {
			Date dateStart = addHours(from, i * hours);
			Date dateEnd = addHours(from, i * hours + hours);
			Future<AnalysisResult> result = analysisScheduler.schedule(company, dateStart, dateEnd);
			futures.add(result);
		}

		int sum = 0;
		double sentimental = 0;
		int timeout = 34567;
		for (Future<AnalysisResult> future : futures) {
			try {
				AnalysisResult result = future.get(timeout, TimeUnit.MILLISECONDS);
				if (Double.isInfinite(result.getResult()) || Double.isNaN(result.getResult())) {
					continue;
				}
				sum += result.getNumberOfTweets();
				sentimental += (result.getResult() * result.getNumberOfTweets());
			} catch (TimeoutException ex) {
				//fail silent
				timeout = 10;
			} catch (InterruptedException ex) {
				ex.printStackTrace();
			} catch (ExecutionException ex) {
				ex.printStackTrace();
			}
		}

		endBus.fire(new AnalysisEndEvent(e.getEventId()));
		logger.info("Finished " + company + ": " + sentimental + " (" + sum + ")");
		double result = sum != 0 ? sentimental / sum : 0.5;
		//return new AsyncResult<Double>(result);
		return result;
	}

	public Integer getNumberOfInstances() {
		return serversOnline;
	}

	public void processServerInstanceEvent(@Observes ServerInstanceChangeEvent event) {
		serversOnline = event.getNumberInstances();
	}
}
