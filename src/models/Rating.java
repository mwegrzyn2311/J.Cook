package models;

import org.bson.types.ObjectId;

import java.util.Date;

public class Rating {
    ObjectId id;
    int stars;
    String description;
    Date date;

    public Rating(ObjectId id, int stars, String description, Date date) {
        this.id = id;
        this.stars = stars;
        this.description = description;
        this.date = date;
    }
}
