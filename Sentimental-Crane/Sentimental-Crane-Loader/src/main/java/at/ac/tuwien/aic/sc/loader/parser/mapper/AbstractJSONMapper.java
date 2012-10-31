package at.ac.tuwien.aic.sc.loader.parser.mapper;

import at.ac.tuwien.aic.sc.loader.parser.mapper.exception.JSONMappingException;
import org.json.JSONException;
import org.json.JSONObject;

/**
 */
public abstract class AbstractJSONMapper<T> implements JSONMapper<T>{
    @Override
    public T map(JSONObject jso) {
        try {
            return mapImpl(jso);
        } catch (JSONException je) {
            throw new JSONMappingException(je);
        }
    }

    protected abstract T mapImpl(JSONObject jso) throws JSONException;
}
