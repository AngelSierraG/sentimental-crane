package at.ac.tuwien.aic.sc.analyzer;

import at.ac.tuwien.aic.sc.core.AnalysisResult;
import at.ac.tuwien.aic.sc.core.AnalysisService;
import at.ac.tuwien.aic.sc.core.entities.Company;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.lang3.StringUtils;
import org.jboss.ejb3.annotation.Clustered;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.enterprise.context.ApplicationScoped;
import javax.sql.DataSource;
import java.sql.*;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Dominik Strasser, dominikstr@gmail.com
 */
@ApplicationScoped
@Stateless
@Remote(AnalysisService.class)
@Clustered
public class TwitterAnalyseService implements AnalysisService {
	private static final Logger logger = Logger.getLogger(TwitterAnalyseService.class.getName());

	@Resource(mappedName = "java:/jdbc/AICDS")
	DataSource dataSource;
	DictionaryService dictionaryService;

	@PostConstruct
	public void start() {
		dictionaryService = DictionaryService.getInstance();

		if (dataSource == null) {
			try {
				Class.forName("com.google.appengine.api.rdbms.AppEngineDriver");
			} catch (ClassNotFoundException e) {
				throw new RuntimeException("No DataSource provided and connection cannot be established.", e);
			}
		}
	}

	@Override
	public AnalysisResult analyse(Company company, Date from, Date to) {
		// Perform same checks
		if (company == null || company.getName() == null || company.getName().trim().isEmpty())
			throw new IllegalArgumentException("Analysis isn't possible because of invalid company");
		if (from == null || to == null)
			throw new IllegalArgumentException("Analysis isn't possible: No date range specified");
		if (from.after(to))
			throw new IllegalArgumentException("Invalid date range: start date is after end date");

		// Okay, parameters seems to be okay - ready to rumble
		long ms = 0;
		if (logger.isLoggable(Level.INFO)) {
			logger.log(Level.INFO, String.format("Performing analysis for company %s(%d); date range is %s to %s",
					company.getName(), company.getId(), from, to));
			ms = System.currentTimeMillis();
		}

		try {
			Connection connection = getConnection();
			PreparedStatement statement = connection.prepareStatement("SELECT text FROM tweet WHERE tweet_date BETWEEN ? AND ?");
			try {
				statement.setDate(1, new java.sql.Date(from.getTime()));
				statement.setDate(2, new java.sql.Date(to.getTime()));
				ResultSet resultSet = statement.executeQuery();
				int posCounter = 0;
				int negCounter = 0;
				while (resultSet.next()) {
					String text = resultSet.getString(1);
					text = text.toLowerCase();

					// Check if the tweet is about the given company
					final String finalText = text;
					if (!CollectionUtils.exists(companyString(company.getName()), new Predicate() {
						@Override
						public boolean evaluate(Object object) {
							return finalText.contains(object.toString());
						}
					})) {
						continue;
					}

					// Process the tweet
					text = text.replaceAll("[.,]", "");
					for (String word : dictionaryService.getStopWords()) {
						text = text.replace(" " + word + " ", " ");
					}

					// Calculate the sentiment value of the tweet regarding the given company
					for (String word : dictionaryService.getGoodWords()) {
						int matches = StringUtils.countMatches(text, word);
						if (matches != 0) {
							posCounter++;
						}
					}
					for (String word : dictionaryService.getBadWords()) {
						int matches = StringUtils.countMatches(text, word);
						if (matches != 0) {
							negCounter++;
						}
					}
				}

				// Calculate the overall sentiment value
				int sentimental = posCounter - negCounter;
				int counter = posCounter + negCounter;
				double result = counter == 0 ? 0 : (double) sentimental / counter;
				result = (result + 1) / 2;

				if (logger.isLoggable(Level.INFO)) {
					logger.log(Level.INFO, String.format("Analysis returned without exception in %dms. Result was: %s",
							System.currentTimeMillis() - ms, result));
				}
				return new AnalysisResult(result, counter);
			} finally {
				statement.close();
				connection.close();
			}
		} catch (SQLException e) {
			logger.log(Level.SEVERE, "Error occurred while fetching data from database", e);
			throw new RuntimeException(e);
		}
	}

	private Connection getConnection() throws SQLException {
		if (dataSource == null) {
			return DriverManager.getConnection("jdbc:google:rdbms://sentimentalcrane-db:sentimentalcranedb/sentimentalcranedb");
		}
		return dataSource.getConnection();
	}

	/**
	 * Returns the words associated with the given company name i.e., product names.
	 *
	 * @param name the company name
	 * @return company-related words
	 */
	private List<String> companyString(String name) {
		return Collections.singletonList(name.toLowerCase());
	}
}
