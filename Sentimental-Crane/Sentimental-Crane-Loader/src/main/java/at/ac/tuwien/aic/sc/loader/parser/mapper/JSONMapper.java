package at.ac.tuwien.aic.sc.loader.parser.mapper;

import org.json.JSONObject;

/**
 */
public interface JSONMapper<T> {
    public T map(JSONObject jso);
}
