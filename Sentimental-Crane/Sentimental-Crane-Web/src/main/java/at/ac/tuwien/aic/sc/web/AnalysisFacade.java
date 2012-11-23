package at.ac.tuwien.aic.sc.web;

import at.ac.tuwien.aic.sc.core.AnalysisResult;
import at.ac.tuwien.aic.sc.core.AnalysisService;
import at.ac.tuwien.aic.sc.core.entities.Company;
import at.ac.tuwien.aic.sc.core.event.AnalysisEndEvent;
import at.ac.tuwien.aic.sc.core.event.AnalysisStartEvent;
import at.ac.tuwien.aic.sc.core.event.ServerInstanceChangeEvent;

import javax.ejb.AsyncResult;
import javax.ejb.Asynchronous;
import javax.ejb.EJB;
import javax.ejb.Schedule;
import javax.ejb.Stateless;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NameClassPair;
import javax.naming.NamingEnumeration;
import java.util.Date;
import java.util.Properties;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Dominik Strasser, dominikstr@gmail.com
 */
@Stateless
public class AnalysisFacade {
	private static final Logger logger = Logger.getLogger(AnalysisFacade.class.getName());

	private AnalysisService service;

	@Inject
	private Event<AnalysisStartEvent> startBus;

	@Inject
	private Event<AnalysisEndEvent> endBus;

	private Integer serversOnline;

	@Asynchronous
	public Future<Double> analyse(Company company, Date from, Date to) {
		try {
			Properties properties = new Properties();
			properties.put(Context.URL_PKG_PREFIXES, "org.jboss.ejb.client.naming");
			properties.put(Context.SECURITY_PRINCIPAL, "ejb");
			properties.put(Context.SECURITY_CREDENTIALS, "test");
			Context ctx = new InitialContext(properties);
			service = (AnalysisService)
					ctx.lookup("ejb:/Sentimental-Crane-Analyzer-1.0-jar-with-dependencies/TwitterAnalyseService!at.ac.tuwien.aic.sc.core.AnalysisService");
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Error in JNDI lookup", e);
		}
		if (company == null)
			throw new IllegalArgumentException("Company hasn't to be null");
		//tell everybody who is interest, we are performing a new analysis
		AnalysisStartEvent e = new AnalysisStartEvent(company.getName(), from, to);
		startBus.fire(e);
		//start analysis in background
		//TODO: maybe we should split up the date range
		AnalysisResult result = service.analyse(company, from, to);
		//send end event
		endBus.fire(new AnalysisEndEvent(e.getEventId()));
		//return the result
		return new AsyncResult<Double>(result.getResult());
	}

	public Integer getNumberOfInstances() {
		return serversOnline;
	}
	
	public void newServerInstanceNumber(@Observes ServerInstanceChangeEvent event) {
		serversOnline = event.getNumberInstances();
		logger.info("New number of server instances: "+serversOnline);
	}
}
