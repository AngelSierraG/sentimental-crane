package at.ac.tuwien.aic.sc.scheduler;

import at.ac.tuwien.aic.sc.core.event.AnalysisEndEvent;
import at.ac.tuwien.aic.sc.core.event.AnalysisStartEvent;
import org.apache.commons.collections.buffer.CircularFifoBuffer;

import javax.ejb.EJB;
import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.enterprise.event.Observes;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

/**
 * @author Dominik Strasser, dominikstr@gmail.com
 */
@Singleton
public class OpenStackScheduler {
	static final Logger logger = Logger.getLogger(OpenStackScheduler.class.getName());
	@EJB
	ClusterManager clusterManager;

	private Map<String, AnalysisStartEvent> startEvents = new ConcurrentHashMap<String, AnalysisStartEvent>();

	private CircularFifoBuffer buffer = new CircularFifoBuffer(10);

	private AtomicBoolean done = new AtomicBoolean(false);

	private long idleTime = 180;

	private long lastAnalysisTime = 0;

	private static final double UPPER_BOUND = 0.30;

	private static final double LOWER_BOUND = 0.18;

	public OpenStackScheduler() {
	}

	public void newAnalysis(@Observes AnalysisStartEvent event) {
		logger.info("New analysis: " + event.getCompanyName() + " (" + event.getFrom() + " - " + event.getTo() + ")");

		lastAnalysisTime = new Date().getTime();
		done.set(false);
		startEvents.put(event.getEventId(), event);
	}

	public void analysisEnded(@Observes AnalysisEndEvent event) {
		logger.info("Analysis ended");
		AnalysisStartEvent startEvent = startEvents.get(event.getStartEventId());
		if (startEvent == null) {
			return;
		}
		double duration = TimeUnit.MILLISECONDS.toSeconds(event.getEventDate().getTime() - startEvent.getEventDate().getTime());
		double hours = TimeUnit.MILLISECONDS.toHours(startEvent.getTo().getTime() - startEvent.getFrom().getTime());

		buffer.add(duration / hours);
	}

	@Schedule(second = "*/30", minute = "*", hour = "*", persistent = false)
	public void run() {
		if (lastAnalysisTime != 0 && TimeUnit.MILLISECONDS.toSeconds(new Date().getTime() - lastAnalysisTime) > idleTime) {
			logger.info("Asking ClusterManager to shutdown one node");
			clusterManager.shutdownClusterNodes(1);
		} else if (!buffer.isEmpty()) {
			if (done.get()) {
				logger.info("Nothing to do because no analysis performed since last run");
				return;
			}
			double score = 0;
			for (Object s : buffer.toArray()) {
				score += (Double) s;
			}
			score = score / buffer.size();

			logger.info("Scheduling score was " + score);

			if (score > UPPER_BOUND) {
				int nodestostart = (int) (score / UPPER_BOUND);
				logger.info("Asking ClusterManager to start " + nodestostart + " nodes");
				clusterManager.startClusterNodes(nodestostart);
			} else if (score < LOWER_BOUND) {
				logger.info("Asking ClusterManager to shutdown one node");
				clusterManager.shutdownClusterNodes(1);
			}
			done.set(true);
		}
	}
}
