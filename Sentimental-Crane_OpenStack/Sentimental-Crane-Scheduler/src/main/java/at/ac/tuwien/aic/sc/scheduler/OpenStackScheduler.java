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

	/**
	 * Contains information about the last analysis runs
	 */
	private CircularFifoBuffer buffer = new CircularFifoBuffer(5);
	/**
	 * {@code true} if the {@link ClusterManager} was invoked since the last analysis.
	 */
	private AtomicBoolean done = new AtomicBoolean(false);
	/**
	 * Number of seconds after the {@link ClusterManager} may shutdown some nodes due to idling.
	 */
	private long idleTime = 300;
	/**
	 * The timestamp of the last analysis.
	 */
	private long lastAnalysisTime = 0;
	/**
	 * If the load is higher than this bound, the {@link ClusterManager} attempts to request new nodes.
	 */
	private static final double UPPER_BOUND = 0.50;
	/**
	 * If the load is lower than this bound, the {@link ClusterManager} attempts to terminate idle nodes.
	 */
	private static final double LOWER_BOUND = 0.35;

	public OpenStackScheduler() {
	}

	public void newAnalysis(@Observes AnalysisStartEvent event) {
		logger.info("New analysis: " + event.getCompanyName() + " (" + event.getFrom() + " - " + event.getTo() + ")");

		lastAnalysisTime = new Date().getTime();
		startEvents.put(event.getEventId(), event);
	}

	public void analysisEnded(@Observes AnalysisEndEvent event) {
		logger.info("Analysis ended");
		AnalysisStartEvent startEvent = startEvents.get(event.getStartEventId());
		if (startEvent == null) {
			return;
		}

		// Update the circular buffer with the statistics of the analysis
		double duration = TimeUnit.MILLISECONDS.toSeconds(event.getEventDate().getTime() - startEvent.getEventDate().getTime());
		double hours = Math.max(24, TimeUnit.MILLISECONDS.toHours(startEvent.getTo().getTime() - startEvent.getFrom().getTime()));

		buffer.add(duration / hours);
		done.set(false);
		run();
	}

	@Schedule(second = "*/30", minute = "*", hour = "*", persistent = false)
	public synchronized void run() {
		// If the cluster is idling some time, tell the ClusterManager to shutdown some nodes
		if (lastAnalysisTime != 0 && TimeUnit.MILLISECONDS.toSeconds(new Date().getTime() - lastAnalysisTime) > idleTime) {
			logger.info("Asking ClusterManager to shutdown some nodes");
			clusterManager.shutdownClusterNodes(2);
			return;
		}

		// If no analysis happened, assume that there is no need to scale the cluster
		if (buffer.isEmpty() || done.get()) {
			logger.info("Nothing to do because no analysis performed since last run");
			return;
		}


		// Calculate the load
		double score = predictClusterLoad();
		if (Double.isInfinite(score) || Double.isNaN(score)) {
			return;
		}


		// Perform a scale up or  scale down depending on the predicted load
		logger.info("Scheduling score was " + score);

		if (score > UPPER_BOUND) {
			// If the score is too high, request some nodes
			int nodesToStart = (int) (score / UPPER_BOUND);
			logger.info("Asking ClusterManager to start " + nodesToStart + " nodes");
			clusterManager.startClusterNodes(nodesToStart);
		} else if (score < LOWER_BOUND) {
			// Otherwise, terminate unused instances
			logger.info("Asking ClusterManager to shutdown one node");
			clusterManager.shutdownClusterNodes(1);
		}
		done.set(true);
	}

	/**
	 * Calculates the cluster load based on the load of the previous analysis runs
	 *
	 * @return the load
	 */
	protected double predictClusterLoad() {
		double score = 0;
		for (Object s : buffer.toArray()) {
			score += (Double) s;
		}
		score = score / buffer.size();
		return score;
	}
}
