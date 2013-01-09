package at.ac.tuwien.aic.sc.loader.parser.mapper.impl;

import at.ac.tuwien.aic.sc.core.entities.Place;
import at.ac.tuwien.aic.sc.loader.parser.mapper.AbstractJSONMapper;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author Bernhard Nickel
 */
public class PlaceMapper extends AbstractJSONMapper<Place> {
	@Override
	protected Place mapImpl(JSONObject jso) throws JSONException {
		String id = jso.getString("id");
		String country = jso.getString("country");
		String countryCode = jso.getString("country_code");
		String type = jso.getString("place_type");
		String name = jso.getString("name");
		String fullName = jso.getString("full_name");


		return new Place(id, country, countryCode, type, name, fullName);
	}
}
