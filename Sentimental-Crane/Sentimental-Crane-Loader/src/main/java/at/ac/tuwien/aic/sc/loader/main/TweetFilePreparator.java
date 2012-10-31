package at.ac.tuwien.aic.sc.loader.main;

import at.ac.tuwien.aic.sc.loader.service.TweetFilePreparationService;

import java.io.*;

/**
 */
public class TweetFilePreparator {
    public static void main(String... args) throws Exception {
        File in = new File("E:/tweets.txt");
        File out = new File("E:/tweets_filtered_compressed.txt");

        TweetFilePreparationService service = new TweetFilePreparationService();
        service.prepareTweetFile(in, out);
    }
}
