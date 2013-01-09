package at.ac.tuwien.aic.sc.loader.main;

import at.ac.tuwien.aic.sc.loader.service.TweetFetchingService;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

/**
 * @author Bernhard Nickel
 */
public class TweetFetcher {

	public static void main(String... args) throws Exception {
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh");
		SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(TweetFetcher.class.getResourceAsStream("mybatis-config.xml"));

		TweetFetchingService tweetFetchingService = new TweetFetchingService(sqlSessionFactory);

		double start = System.currentTimeMillis();
		tweetFetchingService.fetchTweetTexts(dateFormat.parse("2011-07-31 00"), dateFormat.parse("2011-08-01 00"));
		double end = System.currentTimeMillis();

		System.out.println((end - start) / 1000);
	}
}
