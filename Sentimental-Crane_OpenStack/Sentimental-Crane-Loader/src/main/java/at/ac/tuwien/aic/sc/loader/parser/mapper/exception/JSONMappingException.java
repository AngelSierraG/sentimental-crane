package at.ac.tuwien.aic.sc.loader.parser.mapper.exception;

import org.json.JSONException;

import java.text.ParseException;

/**
 * @author Bernhard Nickel
 */
public class JSONMappingException extends RuntimeException {
	public JSONMappingException(JSONException e) {
		super(e);
	}

	public JSONMappingException(ParseException e) {
		super(e);
	}
}
