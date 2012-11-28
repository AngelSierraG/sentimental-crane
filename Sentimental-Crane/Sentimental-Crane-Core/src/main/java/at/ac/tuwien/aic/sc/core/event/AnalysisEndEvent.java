package at.ac.tuwien.aic.sc.core.event;

import java.util.Date;

/**
 * @author Dominik Strasser, dominikstr@gmail.com
 */
public class AnalysisEndEvent {
	private String startEventId;

	private Date eventDate;

	public AnalysisEndEvent(String startEventId) {
		this.startEventId = startEventId;
		this.eventDate=new Date();
	}

	public String getStartEventId() {
		return startEventId;
	}

	public Date getEventDate() {
		return eventDate;
	}
}
