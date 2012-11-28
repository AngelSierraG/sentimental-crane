package at.ac.tuwien.aic.sc.core.event;

import java.util.Date;
import java.util.UUID;

/**
 * @author Dominik Strasser, dominikstr@gmail.com
 */
public class AnalysisStartEvent {
	private String eventId;

	private String companyName;

	private Date from;

	private Date to;

	private Date eventDate;

	public AnalysisStartEvent(String companyName, Date from, Date to) {
		this.companyName = companyName;
		this.from = from;
		this.to = to;
		this.eventId = UUID.randomUUID().toString();
		this.eventDate=new Date();
	}

	public String getEventId() {
		return eventId;
	}

	public String getCompanyName() {
		return companyName;
	}

	public Date getFrom() {
		return from;
	}

	public Date getTo() {
		return to;
	}

	public Date getEventDate() {
		return eventDate;
	}
}
