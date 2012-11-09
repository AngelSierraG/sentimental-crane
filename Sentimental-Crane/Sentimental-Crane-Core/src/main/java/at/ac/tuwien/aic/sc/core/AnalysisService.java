package at.ac.tuwien.aic.sc.core;

import at.ac.tuwien.aic.sc.core.entities.Company;

import javax.ejb.Remote;
import java.util.Date;

/**
 * This service performs the senitmental analysis
 *
 * @author Dominik Strasser, dominikstr@gmail.com
 */
public interface AnalysisService {
	/**
	 * Perform analysis
	 *
	 * @param company the company to analyse
	 * @param from	start date
	 * @param to	  end date
	 * @return result
	 */
	public double analyse(Company company, Date from, Date to);
}
