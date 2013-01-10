package at.ac.tuwien.aic.sc.web;

import javax.ejb.EJB;
import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author Philipp Zeppezauer, philipp.zeppezauer@gmail.com
 */
public class ServerStatusServlet extends HttpServlet {
	private static final long serialVersionUID = -2688112882339771985L;
	@Inject
	@EJB
	private AnalysisFacade facade;

	@Override
	protected void doPost(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws ServletException, IOException {
		doGet(httpServletRequest, httpServletResponse);
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String instances = facade != null ? facade.getNumberOfInstances().toString() : "0";
		response.getWriter().println(instances);
	}
}
