package at.ac.tuwien.aic.sc.web;

import at.ac.tuwien.aic.sc.core.entities.Company;
import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;

import javax.ejb.EJB;
import javax.inject.Inject;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Dominik Strasser, dominikstr@gmail.com
 */
public class AnalysisServlet extends HttpServlet {
	private static final String CURRENT_REQUEST = "CURRENT REQUEST";
	private static final DateFormat DATE_FORMATTER = new SimpleDateFormat("MM/dd/yyyy");
	private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#.#######");

	private static final Logger logger = Logger.getLogger(AnalysisServlet.class.getName());
	@Inject
	@EJB
	private AnalysisFacade facade;

	@Override
	public void init(ServletConfig servletConfig) throws ServletException {
		super.init(servletConfig);

		System.err.println("Attempting to create weld");
		WeldContainer weld = new Weld().initialize();
		System.err.println("Hello weld");
		this.facade = weld.instance().select(AnalysisFacade.class).get();
		System.err.println("Hello facade " + facade);
	}

	@Override
	protected void doPost(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws ServletException, IOException {
		doGet(httpServletRequest, httpServletResponse);
	}

	@Override
	@SuppressWarnings("unchecked")
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		HttpSession session = request.getSession();

		String company = request.getParameter("company");
		String fromParam = request.getParameter("timeFrom");
		String toParam = request.getParameter("timeTo");

		if (company != null && fromParam != null && toParam != null) {
			Date from = null;
			Date to = null;

			try {
				from = DATE_FORMATTER.parse(fromParam);
				to = DATE_FORMATTER.parse(toParam);
			} catch (ParseException p) {
				response.getWriter().println("Error parsing date");
			}

			if (!company.trim().isEmpty()) {
				if (session.getAttribute(CURRENT_REQUEST) != null) {
					response.getWriter().println("Request already running");
					return;
				}

				Company c = new Company();
				c.setName(company);

				session.setAttribute(CURRENT_REQUEST, facade.analyse(c, from, to));
				response.getWriter().println("Started new request");
			}
		} else {
			if (session.getAttribute(CURRENT_REQUEST) != null) {
				Object obj = session.getAttribute(CURRENT_REQUEST);
				if (obj instanceof Future) {
					Future<Double> analysis = (Future<Double>) session.getAttribute(CURRENT_REQUEST);
					if (analysis.isDone()) {
						try {
							response.getWriter().println(DECIMAL_FORMAT.format(analysis.get()));
							session.removeAttribute(CURRENT_REQUEST);
						} catch (Exception e) {
							logger.log(Level.SEVERE, "Error fetching result", e);
							response.setStatus(500);
							response.getWriter().println("!!! Error !!!");
							session.removeAttribute(CURRENT_REQUEST);
						}
					} else {
						response.getWriter().println("Computing...");
					}
				} else {
					double value = (Double) obj;
					response.getWriter().println(DECIMAL_FORMAT.format(value));
					session.removeAttribute(CURRENT_REQUEST);
				}
			} else {
				response.getWriter().println("Nothing to do");
			}
		}
	}
}
