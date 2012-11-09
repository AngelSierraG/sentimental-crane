package at.ac.tuwien.aic.sc.web;

import at.ac.tuwien.aic.sc.core.AnalysisService;
import at.ac.tuwien.aic.sc.core.entities.Company;
import at.ac.tuwien.aic.sc.core.event.AnalysisEndEvent;
import at.ac.tuwien.aic.sc.core.event.AnalysisStartEvent;

import javax.ejb.AsyncResult;
import javax.ejb.Asynchronous;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import java.util.Date;
import java.util.concurrent.Future;

/**
 * @author Dominik Strasser, dominikstr@gmail.com
 */
@Stateless
public class AnalysisFacade {
	//TODO: we should use proper names ;)
	@EJB(lookup = "java:global/Sentimental-Crane-Analyzer-1.0-jar-with-dependencies/TwitterAnalyseService")
	private AnalysisService service;

	@Inject
	private Event<AnalysisStartEvent> startBus;

	@Inject
	private Event<AnalysisEndEvent> endBus;

	@Asynchronous
	public Future<Double> analyse(Company company, Date from, Date to) {
		if (company == null)
			throw new IllegalArgumentException("Company hasn't to be null");
		//tell everybody who is interest, we are performing a new analysis
		AnalysisStartEvent e = new AnalysisStartEvent(company.getName(), from, to);
		startBus.fire(e);
		//start analysis in background
		//TODO: maybe we should split up the date range
		double result = service.analyse(company, from, to);
		//send end event
		endBus.fire(new AnalysisEndEvent(e.getEventId()));
		//return the result
		return new AsyncResult<Double>(result);
	}
}
