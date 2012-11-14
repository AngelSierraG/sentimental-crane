package at.ac.tuwien.aic.sc.analyzer;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Singleton
public class DictionaryService {
	List<String> goodWords;
	List<String> badWords;

	@PostConstruct
	public void buildDefaultLists() {
		goodWords = Arrays.asList("cool", "good");
		badWords = Arrays.asList("bad", "shit", "ugly");
	}

	public List<String> getGoodWords() {
		return Collections.unmodifiableList(goodWords);
	}

	public List<String> getBadWords() {
		return Collections.unmodifiableList(badWords);
	}
}
