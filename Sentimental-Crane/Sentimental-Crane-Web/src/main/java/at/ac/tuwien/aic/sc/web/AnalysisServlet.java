package at.ac.tuwien.aic.sc.web;

import at.ac.tuwien.aic.sc.core.entities.Company;

import javax.ejb.EJB;
import javax.naming.InitialContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.text.ParseException;
import java.util.Date;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Dominik Strasser, dominikstr@gmail.com
 * @editor Kamil El-Isa, elisa.kamil@gmail.com
 */
public class AnalysisServlet extends HttpServlet {
	private static final String CURRENT_REQUEST = "CURRENT REQUEST";
	private static final java.text.DateFormat DATE_FORMATTER = new java.text.SimpleDateFormat("dd.MM.yyyy HH:mm");

	private static final Logger logger = Logger.getLogger(AnalysisServlet.class.getName());

	@EJB
	private AnalysisFacade facade;


	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		if(request.getParameter("start")!=null){
			String company = request.getParameter("company");
			Date from = null;
			Date to = null;
			
			try {
				from = DATE_FORMATTER.parse(request.getParameter("from"));
				to = DATE_FORMATTER.parse(request.getParameter("to"));
			} catch (ParseException e1) {
				response.getWriter().println("Error parsing date");
			}
			
			HttpSession session = request.getSession();
			if (company != null && !company.trim().isEmpty()) {
				if (session.getAttribute(CURRENT_REQUEST) != null) {
					response.getWriter().println("Request already running");
					return;
				}
	
				Company c = new Company();
				c.setName(company);
	
				session.setAttribute(CURRENT_REQUEST, facade.analyse(c, from, to));
				response.getWriter().println("Started new request");
			} else {
				if (session.getAttribute(CURRENT_REQUEST) != null) {
					Future<Double> analysis = (Future<Double>) session.getAttribute(CURRENT_REQUEST);
					if (analysis.isDone()) {
						try {
							response.getWriter().println("Last Anaylsis: " + analysis.get());
							session.removeAttribute(CURRENT_REQUEST);
						} catch (Exception e) {
							logger.log(Level.SEVERE, "Error fetching result", e);
							response.setStatus(500);
							response.getWriter().println("!!! error !!!");
							session.removeAttribute(CURRENT_REQUEST);
						}
					} else {
						response.getWriter().println("running...");
					}
				} else {
					response.getWriter().println("nothing running for you");
				}
			}
		}
	}
}
