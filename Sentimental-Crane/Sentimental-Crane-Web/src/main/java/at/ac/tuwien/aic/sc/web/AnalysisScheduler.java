package at.ac.tuwien.aic.sc.web;

import at.ac.tuwien.aic.sc.core.AnalysisResult;
import at.ac.tuwien.aic.sc.core.AnalysisService;
import at.ac.tuwien.aic.sc.core.entities.Company;

import javax.ejb.AsyncResult;
import javax.ejb.Asynchronous;
import javax.ejb.Stateless;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.ws.rs.core.MediaType;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;

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
	
	private final String URL = "http://2.sentimentalcrane.appspot.com";

	@Asynchronous
	public Future<AnalysisResult> schedule(Company company, Date from, Date to) {
		
		AnalysisResult partialResult = null;
		boolean ws = true;
		
		if(!ws){
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
			partialResult = service.analyse(company, from, to);
		}else{
			try {
			//Get data from the server
			Client client = Client.create();
			WebResource resource = client.resource(URL+"/analyse");
			resource.queryParam("from", from.getTime()+"");
			resource.queryParam("to", to.getTime()+"");
			partialResult = resource.accept(MediaType.APPLICATION_XML).post(AnalysisResult.class, company);
			} catch (Exception e) {
				logger.log(Level.SEVERE, "Error using Restful Service", e);
			}
		}


		return new AsyncResult<AnalysisResult>(partialResult);
	}
	
}
