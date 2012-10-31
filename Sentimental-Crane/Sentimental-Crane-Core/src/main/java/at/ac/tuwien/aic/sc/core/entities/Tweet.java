package at.ac.tuwien.aic.sc.core.entities;

import java.util.Date;

/**
 */
public class Tweet {
    private long id;
    private String text;
    private Date date;
    private Place place;
    private User user;

    public Tweet() {
    }

    public Tweet(long id, String text, Date date, Place place, User user) {
        this.id = id;
        this.text = text;
        this.date = date;
        this.place = place;
        this.user = user;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Place getPlace() {
        return place;
    }

    public void setPlace(Place place) {
        this.place = place;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
