package at.ac.tuwien.aic.sc.web;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author Gregor Schauer
 */
public class EnvironmentUtilsTest {
	@Test
	public void test() {
		assertEquals(true, EnvironmentUtils.isGoogleAppEngine());
	}
}
