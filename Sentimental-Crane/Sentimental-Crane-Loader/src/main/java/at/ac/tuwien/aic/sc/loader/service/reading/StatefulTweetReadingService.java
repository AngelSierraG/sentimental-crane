package at.ac.tuwien.aic.sc.loader.service.reading;

import at.ac.tuwien.aic.sc.core.entities.Tweet;
import at.ac.tuwien.aic.sc.loader.parser.TweetParser;
import at.ac.tuwien.aic.sc.loader.parser.exception.JSONParseException;
import org.apache.log4j.Logger;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.zip.GZIPInputStream;

/**
 */
public class StatefulTweetReadingService {
    private static final Logger logger = Logger.getLogger(StatefulTweetReadingService.class);

    private InputStream in;
    private BufferedReader reader = null;
    private TweetParser tweetParser = new TweetParser();
    private boolean eof = false;

    public StatefulTweetReadingService(InputStream in) {
        this.in = in;
    }

    public List<Tweet> readTweets(int numTweets) throws IOException {
        if (eof) {
            return null;
        }

        if (reader == null) {
            reader = new BufferedReader(new InputStreamReader(new GZIPInputStream(in)));
        }

        if (logger.isInfoEnabled()) {
            logger.info("Reading " + numTweets + " tweets from file");
        }

        List<Tweet> tweets = new ArrayList<Tweet>();
        Set<Long> tweetIds = new TreeSet<Long>();

        for (int i = 0; i < numTweets; i++) {
            String line = reader.readLine();

            if (line == null) {
                eof = true;
                reader.close();
                break;
            }

            Tweet tweet = parseTweet(tweetParser, line);

            if (tweet != null && !tweetIds.contains(tweet.getId())) {
                tweets.add(tweet);
                tweetIds.add(tweet.getId());
            }
        }

        return tweets;
    }

    private Tweet parseTweet(TweetParser tweetParser, String line) {
        try {
            return tweetParser.parse(line);
        } catch (JSONParseException e) {
            logger.warn("Couldn't parse tweet line '" + line + "'", e);
            return null;
        }
    }
}
