package at.ac.tuwien.aic.sc.core.event;

/**
 * @author Dominik Strasser, dominikstr@gmail.com
 */
public class AnalysisEndEvent {
	private String startEventId;

	public AnalysisEndEvent(String startEventId) {
		this.startEventId = startEventId;
	}

	public String getStartEventId() {
		return startEventId;
	}
}
