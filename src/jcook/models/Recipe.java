package jcook.models;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.image.Image;
import jcook.filters.IdFilter;
import jcook.providers.RatingProvider;
import org.bson.codecs.pojo.annotations.BsonProperty;
import org.bson.types.ObjectId;

import java.util.Collection;
import java.util.OptionalDouble;

public class Recipe {
    private String name;

    @BsonProperty(value = "image_path")
    private String imagePath;
    private Collection<Ingredient> ingredients;
    @BsonProperty(value = "ratings")
    private Collection<ObjectId> ratingIds;
    private Collection<String> tags;
    private Collection<Category> categories;

    public Recipe() {
    }

    public Recipe(
            String name,
            String imagePath,
            Collection<Ingredient> ingredients,
            Collection<ObjectId> ratingIds,
            Collection<String> tags,
            Collection<Category> categories) {
        this.name = name;
        this.imagePath = imagePath;
        this.ingredients = ingredients;
        this.ratingIds = ratingIds;
        this.tags = tags;
        this.categories = categories;
    }

    @Override
    public String toString() {
        return this.name;
    }

    public Image getImage() {
        return new Image(getClass().getResourceAsStream(imagePath));
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public Collection<Ingredient> getIngredients() {
        return ingredients;
    }

    public void setIngredients(Collection<Ingredient> ingredients) {
        this.ingredients = ingredients;
    }

    public Collection<ObjectId> getRatingIds() {
        return ratingIds;
    }

    public void setRatingIds(Collection<ObjectId> ratingIds) {
        this.ratingIds = ratingIds;
    }

    public Collection<String> getTags() {
        return tags;
    }

    public void setTags(Collection<String> tags) {
        this.tags = tags;
    }

    public Collection<Category> getCategories() {
        return categories;
    }

    public void setCategories(Collection<Category> categories) {
        this.categories = categories;
    }

    public DoubleProperty getAverageRating() {
        if (ratingIds.isEmpty()) return new SimpleDoubleProperty(0.0);
        Collection<Rating> ratings = RatingProvider.getInstance().getObjects(new IdFilter(ratingIds));
        OptionalDouble sum = ratings.stream().mapToDouble(Rating::getStars).average();
        return new SimpleDoubleProperty(sum.isPresent() ? sum.getAsDouble() : 0.0);
    }
}
