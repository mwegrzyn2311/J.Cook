package filters;

import org.bson.conversions.Bson;

public interface RecipeFilter {
    Bson getQuery();
}
