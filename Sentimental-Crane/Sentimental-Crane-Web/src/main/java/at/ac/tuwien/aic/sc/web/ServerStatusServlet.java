package at.ac.tuwien.aic.sc.web;

import java.io.IOException;
import java.util.logging.Logger;

import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Philipp Zeppezauer, philipp.zeppezauer@gmail.com
 */
public class ServerStatusServlet extends HttpServlet {
	private static final long serialVersionUID = -2688112882339771985L;

	private static final Logger logger = Logger.getLogger(ServerStatusServlet.class.getName());
	
	private AnalysisFacade facade;
	
	@Override
	protected void doPost(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws ServletException, IOException {
		doGet(httpServletRequest, httpServletResponse);
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		//logger.info("Request: Number of active servers");
		response.getWriter().println(facade.getNumberOfInstances().toString());
		//logger.info("Request complete");
		return;
	}
	
}
