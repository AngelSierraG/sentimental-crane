package at.ac.tuwien.aic.sc.loader.mapper;

import at.ac.tuwien.aic.sc.core.entities.User;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;

/**
 * @author Bernhard Nickel
 */
public interface UserMapper {
    @Select("SELECT * FROM tweet_user WHERE id = #{id}")
    public User get(long id);

    @Insert("INSERT INTO tweet_user(id, name, screen_name) VALUES (#{id}, #{name}, #{screenName})")
    public int insert(User user);
}
