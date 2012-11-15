package at.ac.tuwien.aic.sc.analyzer;

import at.ac.tuwien.aic.sc.core.AnalysisResult;
import at.ac.tuwien.aic.sc.core.AnalysisService;
import at.ac.tuwien.aic.sc.core.entities.Company;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Date;
import java.util.List;
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
	@Inject
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
			ResultSet resultSet = statement.executeQuery();
			int posCounter = 0;
			int negCounter = 0;
			int sentimental = 0;
			while (resultSet.next()) {
				String text = resultSet.getString(1);
				text = text.toLowerCase();

				final String finalText = text;
				if (!CollectionUtils.exists(companyString(company.getName()), new Predicate() {
					@Override
					public boolean evaluate(Object object) {
						return finalText.contains(object.toString());
					}
				})) {
					continue;
				}

				text = text.replaceAll("[.,]", "");
				for (String word : dictionaryService.getStopWords()) {
					text = text.replace(" " + word + " ", " ");
				}

				for (String word : dictionaryService.getGoodWords()) {
					int matches = StringUtils.countMatches(text, word);
					sentimental += matches;
					if (matches != 0) {
						posCounter++;
					}
				}
				for (String word : dictionaryService.getBadWords()) {
					int matches = StringUtils.countMatches(text, word);
					sentimental -= matches;
					if (matches != 0) {
						negCounter++;
					}
				}
			}
			/*
			 * TODO: verify that adding up both counters is correct
			 * subtracting them is not correct according to testFormula
			 */
			int counter = Math.abs(posCounter + negCounter);
			double result = counter == 0 ? 0 : (double) sentimental / counter;
			if (logger.isLoggable(Level.INFO)) {
				logger.log(Level.INFO, "Analysis returned without exception in " + (System.currentTimeMillis() - ms) + "ms. Result was: " + result);
			}
			return new AnalysisResult(result, posCounter + negCounter);
		} catch (SQLException e) {
			logger.log(Level.SEVERE, "Error occurred while fetching data from database", e);
			throw new RuntimeException(e);
		}
	}

	private List<String> companyString(String name) {
		return Collections.singletonList(name.toLowerCase());
	}
}
