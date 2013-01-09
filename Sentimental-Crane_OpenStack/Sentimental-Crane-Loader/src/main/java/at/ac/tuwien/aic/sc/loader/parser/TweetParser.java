package at.ac.tuwien.aic.sc.loader.parser;

import at.ac.tuwien.aic.sc.core.entities.Tweet;
import at.ac.tuwien.aic.sc.loader.parser.exception.JSONParseException;
import at.ac.tuwien.aic.sc.loader.parser.mapper.exception.JSONMappingException;
import at.ac.tuwien.aic.sc.loader.parser.mapper.impl.TweetMapper;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

/**
 * @author Bernhard Nickel
 */
public class TweetParser {
	private TweetMapper tweetMapper;

	public TweetParser() {
		tweetMapper = new TweetMapper();
	}

	public Tweet parse(String s) {
		JSONTokener t = new JSONTokener(s);
		try {
			return tweetMapper.map((JSONObject) t.nextValue());
		} catch (JSONException e) {
			throw new JSONParseException(e);
		} catch (JSONMappingException e) {
			throw new JSONParseException(e);
		}
	}
}
