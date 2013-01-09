package at.ac.tuwien.aic.sc.loader.parser.mapper.impl;

import at.ac.tuwien.aic.sc.core.entities.User;
import at.ac.tuwien.aic.sc.loader.parser.mapper.AbstractJSONMapper;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author Bernhard Nickel
 */
public class UserMapper extends AbstractJSONMapper<User> {
	@Override
	protected User mapImpl(JSONObject jso) throws JSONException {
		long id = jso.getLong("id");
		String name = jso.getString("name");
		String screenName = jso.getString("screen_name");

		return new User(id, name, screenName);
	}
}
