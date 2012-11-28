package at.ac.tuwien.aic.sc.web;

import at.ac.tuwien.aic.sc.core.AnalysisResult;
import at.ac.tuwien.aic.sc.core.AnalysisService;
import at.ac.tuwien.aic.sc.core.entities.Company;

import javax.ejb.AsyncResult;
import javax.ejb.Asynchronous;
import javax.ejb.Stateless;
import javax.naming.Context;
import javax.naming.InitialContext;
import java.util.Date;
import java.util.Properties;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Gregor Schauer
 */
@Stateless
public class AnalysisScheduler {
	private static final Logger logger = Logger.getLogger(AnalysisFacade.class.getName());
	private AnalysisService service;

	@Asynchronous
	public Future<AnalysisResult> schedule(Company company, Date from, Date to) {
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


		AnalysisResult partialResult = service.analyse(company, from, to);
		return new AsyncResult<AnalysisResult>(partialResult);
	}
}
