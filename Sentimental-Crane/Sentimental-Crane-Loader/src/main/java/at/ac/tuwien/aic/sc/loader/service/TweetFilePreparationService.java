package at.ac.tuwien.aic.sc.loader.service;

import java.io.*;
import java.util.zip.GZIPOutputStream;

/**
 */
public class TweetFilePreparationService {
    public void prepareTweetFile(File in, File out) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(in)));


        PrintStream gzipOut = new PrintStream(new GZIPOutputStream(new FileOutputStream(out)));

        long counter = 0;

        for (String line = reader.readLine(); line != null; line = reader.readLine()) {
            if (counter++ % 2 != 0) {
                gzipOut.println(line);
            }
        }

        gzipOut.close();
    }
}
