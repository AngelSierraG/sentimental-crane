package at.ac.tuwien.aic.sc.scheduler.support;

import at.ac.tuwien.aic.sc.core.event.AnalysisEndEvent;
import at.ac.tuwien.aic.sc.core.event.AnalysisStartEvent;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class SimpleComputationLoadPredictor implements ComputationLoadPredictor {
	static final Logger logger = Logger.getLogger(ComputationLoadPredictor.class.getName());
	public static final int maxNumberOfClusterNodes = 8;
	Map<String, AnalysisStartEvent> eventMap = new HashMap<String, AnalysisStartEvent>();
	Map<String, AnalysisStartEvent> specialEventMap = new HashMap<String, AnalysisStartEvent>();
	final Date dataBeginDate;
	final Date dataEndDate;
	int days;

	public SimpleComputationLoadPredictor() {
		try {
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
			dataEndDate = format.parse("2011-08-02");
			dataBeginDate = format.parse("2011-07-29");
		} catch (ParseException e) {
			// must not happen
			throw new RuntimeException(e);
		}
	}

	@Override
	public Integer predict(AnalysisStartEvent event) {
		if (event.getFrom().getTime() <= dataBeginDate.getTime() && event.getTo().getTime() >= dataEndDate.getTime()
				|| event.getFrom().getTime() >= dataBeginDate.getTime() && event.getFrom().getTime() <= dataEndDate.getTime()
				|| event.getTo().getTime() >= dataBeginDate.getTime() && event.getTo().getTime() <= dataEndDate.getTime()) {
			specialEventMap.put(event.getEventId(), event);
			logger.info("Detected high load analysis - requesting all computation nodes");
		} else {
			eventMap.put(event.getEventId(), event);
			days += TimeUnit.MILLISECONDS.toDays(event.getTo().getTime() - event.getFrom().getTime());
		}
		return calculateRequiredNodeCount();
	}

	@Override
	public Integer predict(AnalysisEndEvent event) {
		if (specialEventMap.remove(event.getStartEventId()) != null) {
			logger.info("High load analysis ended - " + specialEventMap.size() + " remaining high load analysis");
		} else {
			AnalysisStartEvent startEvent = eventMap.remove(event.getStartEventId());
			days -= TimeUnit.MILLISECONDS.toDays(startEvent.getTo().getTime() - startEvent.getFrom().getTime());
		}
		return calculateRequiredNodeCount();
	}

	public int calculateRequiredNodeCount() {
		if (!specialEventMap.isEmpty()) {
			return maxNumberOfClusterNodes;
		}
		return Math.min(maxNumberOfClusterNodes, Math.max(1, (int) Math.ceil(days / 50.0)));
	}
}
