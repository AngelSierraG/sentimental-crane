package at.ac.tuwien.aic.sc.loader.service;

import at.ac.tuwien.aic.sc.loader.mapper.TweetMapper;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;

import java.util.Date;
import java.util.List;

/**
 * @author Bernhard Nickel
 */
public class TweetFetchingService {
    private SqlSessionFactory sqlSessionFactory;

    public TweetFetchingService() {
    }

    public TweetFetchingService(SqlSessionFactory sqlSessionFactory) {
        this.sqlSessionFactory = sqlSessionFactory;
    }

    public List<String> fetchTweetTexts(Date startdate, Date enddate) {
        SqlSession session = sqlSessionFactory.openSession();
        TweetMapper tweetMapper = session.getMapper(TweetMapper.class);

        List<String> tweets = tweetMapper.getTextList(startdate, enddate);

        session.close();

        return tweets;
    }
}
