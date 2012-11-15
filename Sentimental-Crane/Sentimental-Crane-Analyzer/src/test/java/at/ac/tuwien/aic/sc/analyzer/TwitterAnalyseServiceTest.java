package at.ac.tuwien.aic.sc.analyzer;

import at.ac.tuwien.aic.sc.core.AnalysisResult;
import at.ac.tuwien.aic.sc.core.entities.Company;
import org.hsqldb.jdbc.JDBCDataSource;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.Assert.assertEquals;

public class TwitterAnalyseServiceTest {
	public static final DictionaryService DICTIONARY_SERVICE = new DictionaryService();
	static JDBCDataSource dataSource;

	@BeforeClass
	public static void beforeClass() throws SQLException {
		Logger.getLogger(DICTIONARY_SERVICE.getClass().getName()).setLevel(Level.OFF);
		DICTIONARY_SERVICE.buildDefaultLists();

		dataSource = new JDBCDataSource();
		dataSource.setUrl("jdbc:hsqldb:mem:aname");
		dataSource.setUser("sa");
		Connection connection = dataSource.getConnection();
		connection.createStatement().executeUpdate("CREATE TABLE tweet_user(id BIGINT PRIMARY KEY, name VARCHAR(256), screen_name VARCHAR(256));");
		connection.createStatement().executeUpdate("CREATE TABLE tweet_place(id VARCHAR(256) PRIMARY KEY, country VARCHAR(256), country_code VARCHAR(3), type VARCHAR(64),name VARCHAR(256), full_name VARCHAR(256));");
		connection.createStatement().executeUpdate("CREATE TABLE tweet(id BIGINT PRIMARY KEY, tweet_date TIMESTAMP WITH TIME ZONE, text VARCHAR(512), place_id VARCHAR(256) REFERENCES tweet_place(id), user_id BIGINT REFERENCES tweet_user(id))");
		connection.close();
	}

	@After
	public void deleteData() throws SQLException {
		Connection connection = dataSource.getConnection();
		connection.createStatement().executeUpdate("DELETE FROM tweet");
		connection.close();
	}

	@Test
	public void testEmpty() throws Exception {
		TwitterAnalyseService service = new TwitterAnalyseService();
		service.dataSource = dataSource;

		Company company = new Company("Google");
		AnalysisResult analyse = service.analyse(company, new Date(), new Date());
		assertEquals(0d, analyse.getResult(), 0);
		assertEquals(0, analyse.getNumberOfTweets());
	}

	@Test
	public void testPositiveTweets() throws Exception {
		TwitterAnalyseService service = new TwitterAnalyseService();
		service.dictionaryService = DICTIONARY_SERVICE;
		service.dataSource = dataSource;
		Connection connection = dataSource.getConnection();
		connection.createStatement().executeUpdate("INSERT INTO tweet VALUES (4712, CURRENT_TIMESTAMP, 'Google is good', NULL, NULL)");
		connection.close();

		Company company = new Company("Google");
		AnalysisResult analyse = service.analyse(company, new Date(0), new Date(Long.MAX_VALUE));
		assertEquals(true, analyse.getResult() > 0);
		assertEquals(1, analyse.getNumberOfTweets());
	}

	@Test
	@Ignore
	//TODO: the problem is the not
	public void testNonPositiveSentimental() throws Exception {
		TwitterAnalyseService service = new TwitterAnalyseService();
		service.dictionaryService = DICTIONARY_SERVICE;
		service.dataSource = dataSource;
		Connection connection = dataSource.getConnection();
		connection.createStatement().executeUpdate("INSERT INTO tweet VALUES (4712, CURRENT_TIMESTAMP, 'Google is not cool', NULL, NULL)");
		connection.close();

		Company company = new Company("Google");
		AnalysisResult analyse = service.analyse(company, new Date(0), new Date(Long.MAX_VALUE));
		assertEquals(true, analyse.getResult() < 0);
		assertEquals(1, analyse.getNumberOfTweets());
	}

	@Test
	public void testNeutralTweets() throws Exception {
		TwitterAnalyseService service = new TwitterAnalyseService();
		service.dictionaryService = DICTIONARY_SERVICE;
		service.dataSource = dataSource;
		Connection connection = dataSource.getConnection();
		connection.createStatement().executeUpdate("INSERT INTO tweet VALUES (4712, CURRENT_TIMESTAMP, 'I vistited the Google IO', NULL, NULL)");
		connection.createStatement().executeUpdate("INSERT INTO tweet VALUES (4713, CURRENT_TIMESTAMP, 'Yeah, drinking beer', NULL, NULL)");
		connection.close();

		Company company = new Company("Google");
		AnalysisResult analyse = service.analyse(company, new Date(0), new Date(Long.MAX_VALUE));
		assertEquals(0, analyse.getResult(), 0);
		assertEquals(0, analyse.getNumberOfTweets());

	}

	@Test
	public void testNegativeTweets() throws Exception {
		TwitterAnalyseService service = new TwitterAnalyseService();
		service.dictionaryService = DICTIONARY_SERVICE;
		service.dataSource = dataSource;
		Connection connection = dataSource.getConnection();
		connection.createStatement().executeUpdate("INSERT INTO tweet VALUES (4712, CURRENT_TIMESTAMP, 'Google is bad', NULL, NULL)");
		connection.close();

		Company company = new Company("Google");
		AnalysisResult analyse = service.analyse(company, new Date(0), new Date(Long.MAX_VALUE));
		assertEquals(true, analyse.getResult() < 0);
		assertEquals(1, analyse.getNumberOfTweets());
	}

	@Test
	public void testBigData() throws Exception {
		TwitterAnalyseService service = new TwitterAnalyseService();
		service.dictionaryService = DICTIONARY_SERVICE;
		service.dataSource = dataSource;
		Connection connection = dataSource.getConnection();
		connection.createStatement().executeUpdate("INSERT INTO tweet VALUES (4711, CURRENT_TIMESTAMP, 'Google is good', NULL, NULL)");
		connection.createStatement().executeUpdate("INSERT INTO tweet VALUES (4712, CURRENT_TIMESTAMP, 'Just found a really cool Google product', NULL, NULL)");
		connection.createStatement().executeUpdate("INSERT INTO tweet VALUES (4713, CURRENT_TIMESTAMP, 'An apple a day keeps the doctor away', NULL, NULL)");
		connection.createStatement().executeUpdate("INSERT INTO tweet VALUES (4714, CURRENT_TIMESTAMP, 'Enjoying standing in a queue waiting for the new iPhone', NULL, NULL)");
		connection.createStatement().executeUpdate("INSERT INTO tweet VALUES (4715, CURRENT_TIMESTAMP, 'I like Omas Googlehupf', NULL, NULL)");
		connection.createStatement().executeUpdate("INSERT INTO tweet VALUES (4716, CURRENT_TIMESTAMP, 'Google is evil', NULL, NULL)");
		connection.close();

		Company company = new Company("Google");
		AnalysisResult analyse = service.analyse(company, new Date(0), new Date(Long.MAX_VALUE));
		assertEquals(true, analyse.getResult() > 0);
		assertEquals(4, analyse.getNumberOfTweets());
	}

	@Test
	public void testFormula() throws Exception {
		TwitterAnalyseService service = new TwitterAnalyseService();
		service.dictionaryService = DICTIONARY_SERVICE;
		service.dataSource = dataSource;
		Connection connection = dataSource.getConnection();
		for (int i = 0; i < 10; i++) {
			connection.createStatement().executeUpdate("INSERT INTO tweet VALUES (" + i + ", CURRENT_TIMESTAMP, 'Google is good', NULL, NULL)");
		}
		for (int i = 0; i < 9; i++) {
			connection.createStatement().executeUpdate("INSERT INTO tweet VALUES (" + (100 + i) + ", CURRENT_TIMESTAMP, 'Google is evil', NULL, NULL)");
		}
		connection.close();

		Company company = new Company("Google");
		AnalysisResult analyse = service.analyse(company, new Date(0), new Date(Long.MAX_VALUE));
		assertEquals(true, analyse.getResult() > 0);
		assertEquals(true, analyse.getResult() < 0.5);
		assertEquals(19, analyse.getNumberOfTweets());
	}
}
