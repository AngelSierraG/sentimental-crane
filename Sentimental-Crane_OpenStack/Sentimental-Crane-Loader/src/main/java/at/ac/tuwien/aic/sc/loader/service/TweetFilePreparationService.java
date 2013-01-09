package at.ac.tuwien.aic.sc.loader.service;

import java.io.*;
import java.util.zip.GZIPOutputStream;

/**
 * @author Bernhard Nickel
 */
public class TweetFilePreparationService {
	public void prepareTweetFile(InputStream in, OutputStream out) throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(in));


		PrintStream gzipOut = new PrintStream(new GZIPOutputStream(out));

		long counter = 0;

		for (String line = reader.readLine(); line != null; line = reader.readLine()) {
			if (counter++ % 2 != 0) {
				gzipOut.println(line);
			}
		}

		gzipOut.close();
	}
}
