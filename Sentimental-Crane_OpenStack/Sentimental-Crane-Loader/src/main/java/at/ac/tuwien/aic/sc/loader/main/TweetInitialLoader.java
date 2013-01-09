package at.ac.tuwien.aic.sc.loader.main;

import at.ac.tuwien.aic.sc.loader.service.TweetFilePreparationService;
import at.ac.tuwien.aic.sc.loader.service.TweetInitialLoadingService;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.log4j.Logger;

import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.net.URL;

/**
 * @author Bernhard Nickel
 */
public class TweetInitialLoader {
	private static final Logger logger = Logger.getLogger(TweetInitialLoader.class);

	public static void main(String... args) throws Exception {
		if (args[0] == null) {
			throw new IllegalArgumentException("args[0] = sourcefile");
		}

		final URL url = new URL(args[0]);

		final PipedOutputStream tmpOut = new PipedOutputStream();
		final PipedInputStream tmpIn = new PipedInputStream(tmpOut);


		new Thread(new Runnable() {
			@Override
			public void run() {

				TweetFilePreparationService service = new TweetFilePreparationService();
				try {
					service.prepareTweetFile(url.openStream(), tmpOut);
				} catch (Exception e) {
					logger.error(e);
				}
			}
		}).start();


		new Thread(new Runnable() {
			@Override
			public void run() {
				SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(TweetInitialLoader.class.getResourceAsStream("mybatis-config.xml"));

				TweetInitialLoadingService tweetInitialLoadingService = new TweetInitialLoadingService(sqlSessionFactory);

				try {
					tweetInitialLoadingService.loadTweets(tmpIn);
				} catch (Exception e) {
					logger.error(e);
				}
			}
		}).start();
	}
}
