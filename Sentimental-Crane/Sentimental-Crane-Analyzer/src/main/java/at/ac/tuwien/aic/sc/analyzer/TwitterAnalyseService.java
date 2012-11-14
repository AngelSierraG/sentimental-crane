package at.ac.tuwien.aic.sc.analyzer;

import at.ac.tuwien.aic.sc.core.AnalysisResult;
import at.ac.tuwien.aic.sc.core.AnalysisService;
import at.ac.tuwien.aic.sc.core.entities.Company;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Dominik Strasser, dominikstr@gmail.com
 */
@Stateless
@Remote(AnalysisService.class)
public class TwitterAnalyseService implements AnalysisService {
	private static final Logger logger = Logger.getLogger(TwitterAnalyseService.class.getName());

	@Resource(mappedName = "java:/jdbc/AICDS")
	DataSource dataSource;
	@EJB
	DictionaryService dictionaryService;

	@Override
	public AnalysisResult analyse(Company company, Date from, Date to) {
		//perform same checks
		if (company == null || company.getName() == null || company.getName().trim().isEmpty())
			throw new IllegalArgumentException("Analysis isn't possible because of invalid company");
		if (from == null || to == null)
			throw new IllegalArgumentException("Analysis ins't possible: No date range specified");
		if (from.after(to))
			throw new IllegalArgumentException("Invalid date range: start date is after end date");
		//okay, parameters seems to be okay - ready to rumble
		long ms = 0;
		if (logger.isLoggable(Level.INFO)) {
			logger.log(Level.INFO, "Performing analysis for company " + company.getName() + "(" + company.getId() + "); date range is " + from + " to " + to);
			ms = System.currentTimeMillis();
		}

		try {
			Connection connection = dataSource.getConnection();
			PreparedStatement statement = connection.prepareStatement("SELECT text FROM tweet WHERE tweet_date BETWEEN ? AND ?");
			statement.setDate(1, new java.sql.Date(from.getTime()));
			statement.setDate(2, new java.sql.Date(to.getTime()));
			ResultSet rset = statement.executeQuery();
			int cnt = 0;
			int sentimental = 0;
			while (rset.next()) {
				String text = rset.getString(1);
				for (String word : dictionaryService.getGoodWords()) {
					int matches = StringUtils.countMatches(text, word);
					sentimental += matches;
					if (matches != 0) {
						cnt++;
					}
				}
				for (String word : dictionaryService.getBadWords()) {
					int matches = StringUtils.countMatches(text, word);
					sentimental -= matches;
					if (matches != 0) {
						cnt++;
					}
				}
			}
			double result = cnt == 0 ? 0 : (double) sentimental / cnt;
			if (logger.isLoggable(Level.INFO)) {
				logger.log(Level.INFO, "Analysis returned without exception in " + (System.currentTimeMillis() - ms) + "ms. Result was: " + result);
			}
			return new AnalysisResult(result, cnt);
		} catch (SQLException e) {
			logger.log(Level.SEVERE, "Error occurred while fetching data from database", e);
			throw new RuntimeException(e);
		}
	}
}
