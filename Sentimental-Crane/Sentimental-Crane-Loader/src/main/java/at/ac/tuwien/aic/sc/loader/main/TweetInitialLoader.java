package at.ac.tuwien.aic.sc.loader.main;

import at.ac.tuwien.aic.sc.loader.service.TweetInitialLoadingService;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.File;

/**
 */
public class TweetInitialLoader {
    public static void main (String... args) throws Exception {
        if (args[0] == null) {
            throw new IllegalArgumentException("args[0] = sourcefile");
        }

        File in = new File(args[0]);

        if (!in.exists()) {
            throw new IllegalArgumentException("sourcefile doesn't exist!");
        }

        SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(TweetInitialLoader.class.getResourceAsStream("mybatis-config.xml"));

        TweetInitialLoadingService tweetInitialLoadingService = new TweetInitialLoadingService(sqlSessionFactory);

        tweetInitialLoadingService.loadTweets(in);
    }
}
