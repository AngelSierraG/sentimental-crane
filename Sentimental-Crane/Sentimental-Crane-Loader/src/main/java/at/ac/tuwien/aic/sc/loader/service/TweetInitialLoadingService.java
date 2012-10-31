package at.ac.tuwien.aic.sc.loader.service;

import at.ac.tuwien.aic.sc.core.entities.Tweet;
import at.ac.tuwien.aic.sc.core.mapper.PlaceMapper;
import at.ac.tuwien.aic.sc.core.mapper.TweetMapper;
import at.ac.tuwien.aic.sc.core.mapper.UserMapper;
import at.ac.tuwien.aic.sc.loader.service.reading.StatefulTweetReadingService;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.log4j.Logger;

import java.io.*;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;

/**
 */
public class TweetInitialLoadingService {
    private static final Logger logger = Logger.getLogger(TweetInitialLoadingService.class);

    private static final int TRANSACTION_SIZE = 10000;

    private SqlSessionFactory sqlSessionFactory;

    public TweetInitialLoadingService() {
    }

    public TweetInitialLoadingService(SqlSessionFactory sqlSessionFactory) {
        this.sqlSessionFactory = sqlSessionFactory;
    }

    public void loadTweets(File file) throws IOException {
        Set<Long> userIds = new HashSet<Long>();
        Set<String> placesIds = new HashSet<String>();

        SqlSession session = sqlSessionFactory.openSession(false);

        UserMapper userMapper = session.getMapper(UserMapper.class);
        PlaceMapper placeMapper = session.getMapper(PlaceMapper.class);

        StatefulTweetReadingService readingService = new StatefulTweetReadingService(file);

        for (List<Tweet> tweets = readingService.readTweets(TRANSACTION_SIZE); tweets != null; tweets = readingService.readTweets(TRANSACTION_SIZE)) {
            for (Tweet tweet : tweets) {
                if (!userIds.contains(tweet.getUser().getId())) {
                    userMapper.insert(tweet.getUser());
                    userIds.add(tweet.getUser().getId());
                }

                if (tweet.getPlace() != null && !placesIds.contains(tweet.getPlace().getId())) {
                    placeMapper.insert(tweet.getPlace());
                    placesIds.add(tweet.getPlace().getId());
                }
            }
            //Commit tweet metadata
            session.commit();

            //Save reads
            saveInTransaction(tweets, session);
        }

        session.close();
    }

    private void saveInTransaction(List<Tweet> tweets, SqlSession session) {
        if (logger.isInfoEnabled()) {
            logger.info("Saving " + tweets.size() + " tweets");
        }
        TweetMapper tweetMapper = session.getMapper(TweetMapper.class);

        for (int i = 0; i < tweets.size(); i++) {
            try {
                tweetMapper.insert(tweets.get(i));
            } catch (Exception e) {
                logger.warn("Couldn't save tweet '" + tweets.get(i) + "'", e);
                tweets.remove(i);
                session.rollback();

                saveInTransaction(tweets, session);
                return;
            }
        }
        session.commit();
    }
}
