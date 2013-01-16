package at.ac.tuwien.aic.sc.web;

import at.ac.tuwien.aic.sc.core.AnalysisResult;
import at.ac.tuwien.aic.sc.core.entities.Company;
import at.ac.tuwien.aic.sc.core.event.AnalysisEndEvent;
import at.ac.tuwien.aic.sc.core.event.AnalysisStartEvent;
import at.ac.tuwien.aic.sc.core.event.ServerInstanceChangeEvent;
import org.apache.commons.lang3.time.DateUtils;

import javax.ejb.AsyncResult;
import javax.ejb.Asynchronous;
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
	public static final int DEFAULT_TIMEOUT = 30000;
	@Inject
	@EJB
	AnalysisScheduler analysisScheduler;
	@Inject
	Event<AnalysisStartEvent> startBus;
	@Inject
	Event<AnalysisEndEvent> endBus;
	private Integer serversOnline = 0;

	@Asynchronous
	public Future<Double> analyse(Company company, Date from, Date to) {
		if (company == null) {
			throw new IllegalArgumentException("Company hasn't to be null");
		}

		/*
		 * Process incoming request
		 */

		// Tell everybody who is interested, we are performing a new analysis
		AnalysisStartEvent e = new AnalysisStartEvent(company.getName(), from, to);
		startBus.fire(e);

		// Do some magic scheduling
		scheduleRequest();

		// Calculate time range
		to = DateUtils.setHours(to, 12);
		int hours = Math.max(24, (int) TimeUnit.MILLISECONDS.toHours(to.getTime() - from.getTime())) / 8;
		List<Future<AnalysisResult>> futures = new ArrayList<Future<AnalysisResult>>();


		/*
		 * Map phase: start analysis in the background
		 */
		for (int i = 0; i < 8; i++) {
			Date dateStart = addHours(from, i * hours);
			Date dateEnd = addHours(from, i * hours + hours);
			Future<AnalysisResult> result = analysisScheduler.schedule(company, dateStart, dateEnd);
			futures.add(result);
		}


		/*
		 * Reduce phase: retrieve the results and aggregate the computed sentiment values
		 */
		int sum = 0;
		double sentimental = 0;
		int timeout = DEFAULT_TIMEOUT;
		for (Future<AnalysisResult> future : futures) {
			try {
				AnalysisResult result = future.get(timeout, TimeUnit.MILLISECONDS);
				if (Double.isInfinite(result.getResult()) || Double.isNaN(result.getResult())) {
					continue;
				}
				sum += result.getNumberOfTweets();
				sentimental += (result.getResult() * result.getNumberOfTweets());
			} catch (TimeoutException ex) {
				// fail silently
				// If a timeout occurred, reduce the timeout of pending requests to get the computed values immediately.
				timeout = 10;
			} catch (InterruptedException ex) {
				ex.printStackTrace();
			} catch (ExecutionException ex) {
				ex.printStackTrace();
			}
		}

		// Send out another notification
		endBus.fire(new AnalysisEndEvent(e.getEventId()));
		logger.info("Finished " + company + ": " + sentimental + " (" + sum + ")");
		return new AsyncResult<Double>(sum != 0 ? sentimental / sum : 0.5);
	}

	/**
	 * Schedules a new request.
	 * <p/>
	 * Note that this method simply waits some time to ensure that all components got notified about the request.
	 */
	protected void scheduleRequest() {
		if (!EnvironmentUtils.isGoogleAppEngine()) {
			try {
				Thread.sleep(7000);
			} catch (InterruptedException ex) {
				logger.log(Level.WARNING, "Load balancing interrupted", ex);
			}
		}
	}

	public Integer getNumberOfInstances() {
		return serversOnline;
	}

	public void processServerInstanceEvent(@Observes ServerInstanceChangeEvent event) {
		serversOnline = event.getNumberInstances();
	}
}
