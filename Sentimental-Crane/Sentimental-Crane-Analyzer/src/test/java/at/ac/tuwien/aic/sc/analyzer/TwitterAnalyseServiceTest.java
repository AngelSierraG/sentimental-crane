package at.ac.tuwien.aic.sc.analyzer;

import at.ac.tuwien.aic.sc.core.AnalysisResult;
import at.ac.tuwien.aic.sc.core.entities.Company;
import org.hsqldb.jdbc.JDBCDataSource;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;

import static org.junit.Assert.assertEquals;

public class TwitterAnalyseServiceTest {
	public static final DictionaryService DICTIONARY_SERVICE = new DictionaryService();
	static JDBCDataSource dataSource;

	@BeforeClass
	public static void beforeClass() throws SQLException {
		DICTIONARY_SERVICE.buildDefaultLists();

		dataSource = new JDBCDataSource();
		dataSource.setUrl("jdbc:hsqldb:mem:aname");
		dataSource.setUser("sa");
		Connection connection=dataSource.getConnection();
		connection.createStatement().executeUpdate("CREATE TABLE tweet_user(id BIGINT PRIMARY KEY, name VARCHAR(256), screen_name VARCHAR(256));");
		connection.createStatement().executeUpdate("CREATE TABLE tweet_place(id VARCHAR(256) PRIMARY KEY, country VARCHAR(256), country_code VARCHAR(3), type VARCHAR(64),name VARCHAR(256), full_name VARCHAR(256));");
		connection.createStatement().executeUpdate("CREATE TABLE tweet(id BIGINT PRIMARY KEY, tweet_date TIMESTAMP WITH TIME ZONE, text VARCHAR(512), place_id VARCHAR(256) REFERENCES tweet_place(id), user_id BIGINT REFERENCES tweet_user(id))");
		connection.close();
	}

	@Test
	public void testAnalyse() throws Exception {
		TwitterAnalyseService service = new TwitterAnalyseService();
		service.dataSource = dataSource;

		Company company = new Company();
		company.setName("Google");
		AnalysisResult analyse = service.analyse(company, new Date(), new Date());
		assertEquals(0d, analyse.getResult(), 0);
		assertEquals(0, analyse.getNumberOfTweets());
	}

	@After
	public void deleteData() throws SQLException {
		Connection connection = dataSource.getConnection();
		connection.createStatement().executeUpdate("DELETE FROM tweet");
		connection.close();
	}

	@Test
	public void testPositiveSentimental() throws Exception {
		TwitterAnalyseService service = new TwitterAnalyseService();
		service.dictionaryService = DICTIONARY_SERVICE;
		service.dataSource = dataSource;
		Connection connection = dataSource.getConnection();
		connection.createStatement().executeUpdate("INSERT INTO tweet VALUES (4712, CURRENT_TIMESTAMP, 'Google is good', NULL, NULL)");
		connection.close();

		Company company = new Company();
		company.setName("Google");
		AnalysisResult analyse = service.analyse(company, new Date(0), new Date(Long.MAX_VALUE));
		assertEquals(true, analyse.getResult() > 0);
		assertEquals(1, analyse.getNumberOfTweets());
	}

	@Test
	public void testNegativeSentimental() throws Exception {
		TwitterAnalyseService service = new TwitterAnalyseService();
		service.dictionaryService = DICTIONARY_SERVICE;
		service.dataSource = dataSource;
		Connection connection = dataSource.getConnection();
		connection.createStatement().executeUpdate("INSERT INTO tweet VALUES (4712, CURRENT_TIMESTAMP, 'Google is bad', NULL, NULL)");
		connection.close();

		Company company = new Company();
		company.setName("Google");
		AnalysisResult analyse = service.analyse(company, new Date(0), new Date(Long.MAX_VALUE));
		assertEquals(true, analyse.getResult() < 0);
		assertEquals(1, analyse.getNumberOfTweets());
	}
}
