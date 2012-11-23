package at.ac.tuwien.aic.sc.analyzer;

import javax.management.ObjectName;
import java.lang.management.ManagementFactory;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

//@Singleton
//@Startup
public class DictionaryService implements ConfigurableDictionaryServiceMXBean {
	private static final Logger logger = Logger.getLogger(DictionaryService.class.getName());
	private static DictionaryService instance;

	List<String> goodWords;
	List<String> badWords;
	List<String> stopWords;

	ObjectName name = null;

	public static DictionaryService getInstance() {
		if (instance == null) {
			synchronized (DictionaryService.class) {
				if (instance == null) {
					instance = new DictionaryService();
					instance.buildDefaultLists();
				}
			}
		}
		return instance;
	}

	// @PostConstruct
	public void buildDefaultLists() {
		goodWords = Arrays.asList("cool", "good", "like");
		badWords = Arrays.asList("bad", "shit", "ugly", "evil");
		stopWords = Arrays.asList("and");
		try {
			name = new ObjectName("at.ac.tuwien.aic.sc.analyzer:type=DictionaryService");
			ManagementFactory.getPlatformMBeanServer().registerMBean(this, name);
			logger.info("Successfully registered MBean DictionaryService");
		} catch (Exception e) {
			logger.log(Level.WARNING, "Error registering service", e);
		}
	}

	// @PreDestroy
	public void removeFromJMX() {
		try {
			ManagementFactory.getPlatformMBeanServer().unregisterMBean(name);
		} catch (Exception e) {
			logger.log(Level.WARNING, "Error unregistering service", e);
		}
	}

	public List<String> getGoodWords() {
		return Collections.unmodifiableList(goodWords);
	}

	public List<String> getBadWords() {
		return Collections.unmodifiableList(badWords);
	}

	public List<String> getStopWords() {
		return Collections.unmodifiableList(stopWords);
	}

	@Override
	public void addGoodWord(String goodWord) {
		addIfNotExists(goodWords, goodWord);
	}

	@Override
	public void addBadWord(String badWord) {
		addIfNotExists(badWords, badWord);
	}

	@Override
	public void addStopWord(String stopWord) {
		addIfNotExists(stopWords, stopWord);
	}

	private void addIfNotExists(List<String> list, String word) {
		if (!list.contains(word)) {
			list.add(word);
		}
	}

	@Override
	public void clearGoodWords() {
		goodWords.clear();
	}

	@Override
	public void clearBadWords() {
		badWords.clear();
	}

	@Override
	public void clearStopWords() {
		stopWords.clear();
	}
}
