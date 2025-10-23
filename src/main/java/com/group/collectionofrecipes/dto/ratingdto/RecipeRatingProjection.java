package com.group.collectionofrecipes.dto.ratingdto;


public interface RecipeRatingProjection {
    Long getId();
    Double getAverageRating();
    Long getCountOfRating();
}
