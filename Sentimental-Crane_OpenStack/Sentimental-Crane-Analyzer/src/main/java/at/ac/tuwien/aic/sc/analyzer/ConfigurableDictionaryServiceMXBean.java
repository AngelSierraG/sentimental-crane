package at.ac.tuwien.aic.sc.analyzer;

import java.util.List;

/**
 * @author Dominik Strasser, dominikstr@gmail.com
 */
public interface ConfigurableDictionaryServiceMXBean {
	public void addGoodWord(String goodWord);

	public void addBadWord(String badWord);

	public void addStopWord(String stopWord);

	public List<String> getGoodWords();

	public List<String> getBadWords();

	public List<String> getStopWords();

	public void clearGoodWords();

	public void clearBadWords();

	public void clearStopWords();
}
