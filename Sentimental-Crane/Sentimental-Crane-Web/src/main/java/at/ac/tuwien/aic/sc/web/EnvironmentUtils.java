package at.ac.tuwien.aic.sc.web;

/**
 * @author Gregor Schauer
 */
public class EnvironmentUtils {
	private static Boolean googleAppEngine;

	private EnvironmentUtils() {
	}

	public static boolean isGoogleAppEngine() {
		if (googleAppEngine == null) {
			try {
				Class.forName("com.google.appengine.api.rdbms.AppEngineDriver");
				googleAppEngine = true;
			} catch (ClassNotFoundException e) {
				googleAppEngine = false;
			}
		}
		return googleAppEngine;
	}
}
