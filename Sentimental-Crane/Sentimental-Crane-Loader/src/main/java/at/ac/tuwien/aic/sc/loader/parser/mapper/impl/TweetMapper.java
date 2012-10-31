package at.ac.tuwien.aic.sc.loader.parser.mapper.impl;

import at.ac.tuwien.aic.sc.core.entities.Place;
import at.ac.tuwien.aic.sc.core.entities.Tweet;
import at.ac.tuwien.aic.sc.core.entities.User;
import at.ac.tuwien.aic.sc.loader.parser.mapper.AbstractJSONMapper;
import at.ac.tuwien.aic.sc.loader.parser.mapper.JSONMapper;
import at.ac.tuwien.aic.sc.loader.parser.mapper.exception.JSONMappingException;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 */
public class TweetMapper extends AbstractJSONMapper<Tweet>{
    private static final String TWEET_DATE_FORMAT = "EEE MMM dd HH:mm:ss Z yyyy";

    private PlaceMapper placeMapper;
    private UserMapper userMapper;

    private DateFormat dateFormat;

    public TweetMapper() {
        placeMapper = new PlaceMapper();
        userMapper = new UserMapper();

        dateFormat = new SimpleDateFormat(TWEET_DATE_FORMAT, Locale.ENGLISH);
    }

    @Override
    protected Tweet mapImpl(JSONObject jso) throws JSONException {
        //Mandatory fields
        long id = jso.getLong("id");
        String text = jso.getString("text");
        Date date = getTweetDate(jso);

        User user = userMapper.map(jso.getJSONObject("user"));

        //Optional fields
        Place place = map(jso, "place", placeMapper);

        return new Tweet(id, text, date, place, user);
    }

    private Date getTweetDate(JSONObject jso) {
      try {
            return dateFormat.parse(jso.getString("created_at"));
        } catch (JSONException e) {
            throw new JSONMappingException(e);
        } catch (ParseException e) {
            throw new JSONMappingException(e);
        }
    }

    private <T> T map(JSONObject jso, String attribute, JSONMapper<T> mapper) throws JSONException {
        if (jso.has(attribute) && !jso.isNull(attribute)) {
            return mapper.map(jso.getJSONObject(attribute));
        } else {
            return null;
        }
    }
}
