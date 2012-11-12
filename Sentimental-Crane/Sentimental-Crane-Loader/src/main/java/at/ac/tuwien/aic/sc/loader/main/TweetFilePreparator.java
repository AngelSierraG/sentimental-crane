package at.ac.tuwien.aic.sc.loader.main;

import at.ac.tuwien.aic.sc.loader.service.TweetFilePreparationService;

import java.io.*;

/**
 */
public class TweetFilePreparator {
    public static void main(String... args) throws Exception {
        if (args[0] == null ||args[1] == null) {
            throw new IllegalArgumentException("args[0] = sourcefile, args[1] = destfile");
        }

        File in = new File(args[0]);

        if (!in.exists()) {
            throw new IllegalArgumentException("sourcefile doesn't exist!");
        }


        File out = new File(args[1]);

        TweetFilePreparationService service = new TweetFilePreparationService();
        service.prepareTweetFile(in, out);
    }
}
