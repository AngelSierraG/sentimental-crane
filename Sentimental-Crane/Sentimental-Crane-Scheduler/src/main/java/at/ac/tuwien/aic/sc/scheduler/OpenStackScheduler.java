package at.ac.tuwien.aic.sc.scheduler;

import at.ac.tuwien.aic.sc.core.event.AnalysisEndEvent;
import at.ac.tuwien.aic.sc.core.event.AnalysisStartEvent;

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
	Map<String, AnalysisStartEvent> eventMap = new HashMap<String, AnalysisStartEvent>();
	Map<String, AnalysisStartEvent> specialEventMap = new HashMap<String, AnalysisStartEvent>();
	final int maxNumberOfClusterNodes = 8;
	Date dataBeginDate;
	Date dataEndDate;
	long days = 0;
	@EJB
	ClusterManager clusterManager;

	public OpenStackScheduler() {
	}

	@PostConstruct
	public void init() throws ParseException {
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		dataEndDate = format.parse("2011-08-02");
		dataBeginDate = format.parse("2011-07-29");
	}

	public void newAnalysis(@Observes AnalysisStartEvent event) {
		logger.info("New analysis: " + event.getCompanyName() + " (" + event.getFrom() + " - " + event.getTo() + ")");

		int currentNodeCount = clusterManager.getNumberOfRunningNodes();

		if (event.getFrom().getTime() <= dataBeginDate.getTime() && event.getTo().getTime() >= dataEndDate.getTime()
				|| event.getFrom().getTime() >= dataBeginDate.getTime() && event.getFrom().getTime() <= dataEndDate.getTime()
				|| event.getTo().getTime() >= dataBeginDate.getTime() && event.getTo().getTime() <= dataEndDate.getTime()) {
			specialEventMap.put(event.getEventId(), event);
			logger.info("Detected high load analysis - requesting all computation nodes");
		} else {
			eventMap.put(event.getEventId(), event);
			days += TimeUnit.MILLISECONDS.toDays(event.getTo().getTime() - event.getFrom().getTime());
		}

		int requiredNodeCount = calculateRequiredNodeCount();
		if (requiredNodeCount > currentNodeCount) {
			logger.info("Currently " + currentNodeCount + " nodes are available - " + requiredNodeCount + " nodes are required.");
		}
		clusterManager.startClusterNodes(requiredNodeCount - currentNodeCount);
	}

	protected int calculateRequiredNodeCount() {
		if (!specialEventMap.isEmpty()) {
			return maxNumberOfClusterNodes;
		}
		return Math.min(maxNumberOfClusterNodes, Math.max(1, (int) Math.ceil(days / 1000)));
	}

	public void analysisEnded(@Observes AnalysisEndEvent event) {
		logger.info("Analysis ended");

		if (specialEventMap.remove(event.getStartEventId()) != null) {
			logger.info("High load analysis ended - " + specialEventMap.size() + " remaining high load analysis");
		} else {
			AnalysisStartEvent startEvent = eventMap.remove(event.getStartEventId());
			days -= TimeUnit.MILLISECONDS.toDays(startEvent.getTo().getTime() - startEvent.getFrom().getTime());
		}

		int currentNodeCount = clusterManager.getNumberOfRunningNodes();
		int requiredNodeCount = calculateRequiredNodeCount();
		if (currentNodeCount > requiredNodeCount) {
			logger.info("Currently " + currentNodeCount + " nodes are available - " + requiredNodeCount + " nodes are required.");
		}
		clusterManager.shutdownClusterNodes(currentNodeCount - requiredNodeCount);
	}
}
