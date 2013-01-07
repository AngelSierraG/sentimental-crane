package at.ac.tuwien.aic.sc.core;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class AnalysisResult implements Serializable{
	double result;
	int numberOfTweets;

	public AnalysisResult() {
	}

	public AnalysisResult(double result, int numberOfTweets) {
		this.result = result;
		this.numberOfTweets = numberOfTweets;
	}

	public double getResult() {
		return result;
	}

	public int getNumberOfTweets() {
		return numberOfTweets;
	}
}
