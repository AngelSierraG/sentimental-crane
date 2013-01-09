package at.ac.tuwien.aic.sc.loader.mapper;

import at.ac.tuwien.aic.sc.core.entities.Place;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;

/**
 * @author Bernhard Nickel
 */
public interface PlaceMapper {
	@Select("SELECT * FROM tweet_place WHERE id = #{id}")
	public Place get(String id);

	@Insert("INSERT INTO tweet_place(id, country, country_code, type, name, full_name) VALUES (#{id}, #{country}, #{countryCode}, #{type}, #{name}, #{fullName})")
	public int insert(Place place);
}
