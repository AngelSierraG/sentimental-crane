package at.ac.tuwien.aic.sc.scheduler;

import at.ac.tuwien.aic.sc.core.event.AnalysisEndEvent;
import at.ac.tuwien.aic.sc.core.event.AnalysisStartEvent;

import javax.ejb.EJB;
import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.enterprise.event.Observes;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * @author Dominik Strasser, dominikstr@gmail.com
 */
@Singleton
public class OpenStackScheduler {
	static final Logger logger = Logger.getLogger(OpenStackScheduler.class.getName());
	@EJB
	ClusterManager clusterManager;

	Date lastAnalysisDate = null;

	int idleTime = 60;

	public OpenStackScheduler() {
	}

	public void newAnalysis(@Observes AnalysisStartEvent event) {
		logger.info("New analysis: " + event.getCompanyName() + " (" + event.getFrom() + " - " + event.getTo() + ")");
		lastAnalysisDate = new Date();

		int currentNodeCount = clusterManager.getNumberOfRunningNodes();

		clusterManager.startClusterNodes(8 - currentNodeCount);
	}

	public void analysisEnded(@Observes AnalysisEndEvent event) {
		logger.info("Analysis ended");
	}

	@Schedule(second = "*/20", minute = "*", hour = "*", persistent = false)
	public void run() {
		if (lastAnalysisDate != null && (TimeUnit.MILLISECONDS.toSeconds(new Date().getTime() - lastAnalysisDate.getTime()) > idleTime)) {
			clusterManager.shutdownClusterNodes(1);
		}
	}
}
