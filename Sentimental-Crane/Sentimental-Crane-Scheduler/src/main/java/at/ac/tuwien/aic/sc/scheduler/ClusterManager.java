package at.ac.tuwien.aic.sc.scheduler;

import javax.ejb.Asynchronous;
import javax.ejb.Stateless;
import java.util.logging.Logger;

/**
 * @author Dominik Strasser, dominikstr@gmail.com
 */
@Stateless
public class ClusterManager {
	private static final Logger logger = Logger.getLogger(ClusterManager.class.getName());

	@Asynchronous
	public void startClusterNode() {
		logger.info("Starting up cluster node");
	}

	@Asynchronous
	public void shutdownClusterNode() {
		logger.info("shutting down cluster node");
	}

	public int getNumberOfRunningNodes() {
		return 0;
	}
}
