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
import javax.sql.DataSource;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

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
@Path("/analyse")
@Stateless
@Remote(AnalysisService.class)
@Clustered
public class TwitterAnalyseService implements AnalysisService {
	private static final Logger logger = Logger.getLogger(TwitterAnalyseService.class.getName());

	@Resource(mappedName = "java:/jdbc/AICDS")
	DataSource dataSource;
	// @EJB
	DictionaryService dictionaryService;

	@PostConstruct
	public void start() {
		System.out.println("Starting up service");
		dictionaryService = DictionaryService.getInstance();
	}

	@POST
	@Produces(MediaType.APPLICATION_XML)
	@Consumes(MediaType.APPLICATION_XML)
	public AnalysisResult analyse(
			Company company, 
			@QueryParam("from") long from, 
			@QueryParam("to") long to) {
		Date fromDate = new Date(from);
		Date toDate = new Date(to);
		return analyse(company, fromDate, toDate);
	}
	

	@Override
	public AnalysisResult analyse(Company company, Date from, Date to) {
		//perform same checks
		if (company == null || company.getName() == null || company.getName().trim().isEmpty())
			throw new IllegalArgumentException("Analysis isn't possible because of invalid company");
		if (from == null || to == null)
			throw new IllegalArgumentException("Analysis isn't possible: No date range specified");
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
			try {
				statement.setDate(1, new java.sql.Date(from.getTime()));
				statement.setDate(2, new java.sql.Date(to.getTime()));
				ResultSet resultSet = statement.executeQuery();
				int posCounter = 0;
				int negCounter = 0;
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

				/*
				 * TODO: verify that adding up both counters is correct
				 * subtracting them is not correct according to testFormula
				 */
				int sentimental = posCounter - negCounter;
				int counter = posCounter + negCounter;
				double result = counter == 0 ? 0 : (double) sentimental / counter;
				result = (result + 1) / 2;
				if (logger.isLoggable(Level.INFO)) {
					logger.log(Level.INFO, "Analysis returned without exception in " + (System.currentTimeMillis() - ms) + "ms. Result was: " + result);
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

	private List<String> companyString(String name) {
		return Collections.singletonList(name.toLowerCase());
	}
}
