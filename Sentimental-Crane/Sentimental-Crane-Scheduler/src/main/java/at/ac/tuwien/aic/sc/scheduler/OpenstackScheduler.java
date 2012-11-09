package at.ac.tuwien.aic.sc.scheduler;

import at.ac.tuwien.aic.sc.core.event.AnalysisEndEvent;
import at.ac.tuwien.aic.sc.core.event.AnalysisStartEvent;

import javax.ejb.EJB;
import javax.ejb.Singleton;
import javax.ejb.Stateless;
import javax.enterprise.event.Observes;
import java.util.logging.Logger;

/**
 * @author Dominik Strasser, dominikstr@gmail.com
 */
@Singleton
public class OpenstackScheduler {
	@EJB
	private ClusterManager clusterManager;

	private static final Logger logger = Logger.getLogger(OpenstackScheduler.class.getName());

	public void newAnalysis(@Observes AnalysisStartEvent event) {
		logger.info("New analysis: " + event.getCompanyName() + " (" + event.getFrom() + " - " + event.getTo() + ")");
	}

	public void analysisEnded(@Observes AnalysisEndEvent event) {
		logger.info("Analysis ended");
	}
}
