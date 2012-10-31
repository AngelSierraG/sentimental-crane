package at.ac.tuwien.aic.sc.loader.main;

import at.ac.tuwien.aic.sc.loader.service.TweetInitialLoadingService;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.File;

/**
 */
public class TweetInitialLoader {
    public static void main (String... args) throws Exception {
        SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(TweetInitialLoader.class.getResourceAsStream("mybatis-config.xml"));

        File file = new File("E:/tweets_filtered_compressed.txt");

        TweetInitialLoadingService tweetInitialLoadingService = new TweetInitialLoadingService(sqlSessionFactory);

        tweetInitialLoadingService.loadTweets(file);
    }
}
