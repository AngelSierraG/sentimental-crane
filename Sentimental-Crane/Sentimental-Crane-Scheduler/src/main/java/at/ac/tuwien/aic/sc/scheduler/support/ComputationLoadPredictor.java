package at.ac.tuwien.aic.sc.scheduler.support;

import at.ac.tuwien.aic.sc.core.event.AnalysisEndEvent;
import at.ac.tuwien.aic.sc.core.event.AnalysisStartEvent;

public interface ComputationLoadPredictor {
	Integer predict(AnalysisStartEvent event);

	Integer predict(AnalysisEndEvent event);
}
