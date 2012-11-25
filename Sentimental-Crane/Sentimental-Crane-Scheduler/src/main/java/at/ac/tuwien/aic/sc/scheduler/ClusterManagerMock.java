package at.ac.tuwien.aic.sc.scheduler;

import java.util.logging.Logger;

public class ClusterManagerMock extends ClusterManager {
	private static final Logger logger = Logger.getLogger(ClusterManagerMock.class.getName());
	int nodeCount = 0;

	@Override
	public void startClusterNodes(int n) {
		nodeCount += n;
		logger.info("Starting " + n + " nodes - " + getNumberOfRunningNodes() + " available");
	}

	@Override
	public void shutdownClusterNodes(int n) {
		nodeCount -= n;
		logger.info("Stopping " + n + " nodes - " + getNumberOfRunningNodes() + " remaining");
	}

	@Override
	public int getNumberOfRunningNodes() {
		return nodeCount;
	}

	@Override
	public void fireServerInstanceChangeEvent() {
		throw new UnsupportedOperationException();
	}
}
