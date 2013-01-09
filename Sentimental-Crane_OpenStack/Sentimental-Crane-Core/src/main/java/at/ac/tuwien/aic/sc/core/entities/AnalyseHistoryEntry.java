package at.ac.tuwien.aic.sc.core.entities;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

/**
 * @author Dominik Strasser, dominikstr@gmail.com
 */
@Entity
public class AnalyseHistoryEntry implements Serializable {
	@Id
	@GeneratedValue
	private long id;

	@Temporal(TemporalType.TIMESTAMP)
	private Date creationDate = new Date();

	@Temporal(TemporalType.DATE)
	private Date searchFrom;

	@Temporal(TemporalType.DATE)
	private Date searchTo;

	private double result;

	public AnalyseHistoryEntry() {
	}

	public AnalyseHistoryEntry(Date searchFrom, Date searchTo) {
		this.searchFrom = searchFrom;
		this.searchTo = searchTo;
	}

	public long getId() {
		return id;
	}

	public Date getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	public Date getSearchFrom() {
		return searchFrom;
	}

	public void setSearchFrom(Date searchFrom) {
		this.searchFrom = searchFrom;
	}

	public Date getSearchTo() {
		return searchTo;
	}

	public void setSearchTo(Date searchTo) {
		this.searchTo = searchTo;
	}

	public double getResult() {
		return result;
	}

	public void setResult(double result) {
		this.result = result;
	}
}
