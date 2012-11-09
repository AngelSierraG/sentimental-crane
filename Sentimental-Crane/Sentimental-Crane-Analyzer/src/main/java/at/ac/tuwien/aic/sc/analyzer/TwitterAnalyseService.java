package at.ac.tuwien.aic.sc.analyzer;

import at.ac.tuwien.aic.sc.core.AnalysisService;
import at.ac.tuwien.aic.sc.core.entities.Company;

import javax.ejb.Remote;
import javax.ejb.Stateless;
import java.util.Date;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Dominik Strasser, dominikstr@gmail.com
 */
@Stateless
@Remote(AnalysisService.class)
public class TwitterAnalyseService implements AnalysisService {
	private static final Logger logger = Logger.getLogger(TwitterAnalyseService.class.getName());

	@Override
	public double analyse(Company company, Date from, Date to) {
		//perform same checks
		if (company == null || company.getName() == null || company.getName().trim().isEmpty())
			throw new IllegalArgumentException("Analysis isn't possible because of invalid company");
		if (from == null || to == null)
			throw new IllegalArgumentException("Analysis ins't possible: No date range specified");
		if (from.after(to))
			throw new IllegalArgumentException("Invalid date range: start date is after end date");
		//okay, paramters seems to be okay - ready to rumble
		long ms = 0;
		if (logger.isLoggable(Level.INFO)) {
			logger.log(Level.INFO, "Performing analysis for company " + company.getName() + "(" + company.getId() + "); date range is " + from + " to " + to);
			ms = System.currentTimeMillis();
		}
		//TODO: implement!
		double result = new Random().nextDouble() * 10;
		try {
			Thread.sleep((long) (result * 10));
		} catch (InterruptedException e) {
			//is okay
		}
		if (logger.isLoggable(Level.INFO)) {
			logger.log(Level.INFO, "Analysis returned without exception in " + (System.currentTimeMillis() - ms) + "ms. Result was: " + result);
		}
		return result;
	}
}
