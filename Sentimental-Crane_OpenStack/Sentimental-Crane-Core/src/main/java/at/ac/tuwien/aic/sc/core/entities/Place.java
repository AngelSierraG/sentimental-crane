package at.ac.tuwien.aic.sc.core.entities;

/**
 * @author Bernhard Nickel
 */
public class Place {
    private String id;
    private String country;
    private String countryCode;
    private String type;
    private String name;
    private String fullName;

    public Place() {
    }

    public Place(String id, String country, String countryCode, String type, String name, String fullName) {
        this.id = id;
        this.country = country;
        this.countryCode = countryCode;
        this.type = type;
        this.name = name;
        this.fullName = fullName;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
}
