package at.ac.tuwien.aic.sc.core.entities;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlRootElement;

import java.io.Serializable;
import java.util.List;

/**
 * @author Dominik Strasser, dominikstr@gmail.com
 */
@Entity
@XmlRootElement
public class Company implements Serializable {
	@Id
	@GeneratedValue
	private int id;

	private String name;

	@OneToMany
	@OrderBy("creationDate DESC")
	private List<AnalyseHistoryEntry> history;

	public Company() {
	}

	public Company(String name) {
		this.name = name;
	}

	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<AnalyseHistoryEntry> getHistory() {
		return history;
	}

	public void setHistory(List<AnalyseHistoryEntry> history) {
		this.history = history;
	}

	@Override
	public String toString() {
		return getName();
	}
}
