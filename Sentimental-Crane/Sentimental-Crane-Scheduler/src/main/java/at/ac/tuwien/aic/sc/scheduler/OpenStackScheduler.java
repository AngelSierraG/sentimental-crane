package at.ac.tuwien.aic.sc.scheduler;

import at.ac.tuwien.aic.sc.core.event.AnalysisEndEvent;
import at.ac.tuwien.aic.sc.core.event.AnalysisStartEvent;
import at.ac.tuwien.aic.sc.scheduler.support.ComputationLoadPredictor;
import at.ac.tuwien.aic.sc.scheduler.support.SimpleComputationLoadPredictor;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.Singleton;
import javax.enterprise.event.Observes;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * @author Dominik Strasser, dominikstr@gmail.com
 */
@Singleton
public class OpenStackScheduler {
	static final Logger logger = Logger.getLogger(OpenStackScheduler.class.getName());
	ComputationLoadPredictor loadPredictor = new SimpleComputationLoadPredictor();
	@EJB
	ClusterManager clusterManager;

	public OpenStackScheduler() {
	}

	public void newAnalysis(@Observes AnalysisStartEvent event) {
		logger.info("New analysis: " + event.getCompanyName() + " (" + event.getFrom() + " - " + event.getTo() + ")");

		int currentNodeCount = clusterManager.getNumberOfRunningNodes();
		int requiredNodeCount = loadPredictor.predict(event);

		logger.info("Currently " + currentNodeCount + " nodes are available - " + requiredNodeCount + " nodes are required.");
		if (requiredNodeCount > currentNodeCount) {
			clusterManager.startClusterNodes(requiredNodeCount - currentNodeCount);
		}
	}

	public void analysisEnded(@Observes AnalysisEndEvent event) {
		logger.info("Analysis ended");

		int currentNodeCount = clusterManager.getNumberOfRunningNodes();
		int requiredNodeCount = loadPredictor.predict(event);

		logger.info("Currently " + currentNodeCount + " nodes are available - " + requiredNodeCount + " nodes are required.");
		if (currentNodeCount > requiredNodeCount) {
			clusterManager.shutdownClusterNodes(currentNodeCount - requiredNodeCount);
		}
	}
}
