package at.ac.tuwien.aic.sc.loader.mapper;

import at.ac.tuwien.aic.sc.core.entities.Place;
import at.ac.tuwien.aic.sc.core.entities.Tweet;
import at.ac.tuwien.aic.sc.core.entities.User;
import org.apache.ibatis.annotations.*;

import java.util.Date;
import java.util.List;

/**
 * @author Bernhard Nickel
 */
public interface TweetMapper {
    @Insert("INSERT INTO tweet(id, text, tweet_date, place_id, user_id) VALUES (#{id}, #{text}, #{date}, #{place.id}, #{user.id})")
    public int insert(Tweet tweet);

    @Select("SELECT * FROM tweet WHERE id = #{id}")
    @Results(value = {
            @Result(property = "id", column = "id"),
            @Result(property = "date", column = "tweet_date"),
            @Result(property = "text", column = "text"),
            @Result(property = "place", column = "place_id", javaType = Place.class, one = @One(select = "at.ac.tuwien.aic.tweetloader.mapper.PlaceMapper.get")),
            @Result(property = "user", column = "user_id", javaType = User.class, one = @One(select = "at.ac.tuwien.aic.tweetloader.mapper.UserMapper.get"))
    })
    @Options(fetchSize = 1)
    public Tweet get(long id);

    @Select("SELECT text FROM tweet WHERE tweet_date BETWEEN #{param1} AND #{param2}")
    @Options(fetchSize = 5000)
    public List<String> getTextList(Date startdate, Date enddate);
}
