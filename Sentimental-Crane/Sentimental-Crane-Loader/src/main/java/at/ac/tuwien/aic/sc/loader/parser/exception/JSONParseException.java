package at.ac.tuwien.aic.sc.loader.parser.exception;

import at.ac.tuwien.aic.sc.loader.parser.mapper.exception.JSONMappingException;
import org.json.JSONException;

/**
 */
public class JSONParseException extends RuntimeException {
    public JSONParseException(JSONException e) {
        super(e);
    }

    public JSONParseException(JSONMappingException e) {
        super(e);
    }
}
