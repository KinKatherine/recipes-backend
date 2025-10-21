package com.group.collectionofrecipes.mappers;

import com.group.collectionofrecipes.dto.ratingdto.CreateRatingDTO;
import com.group.collectionofrecipes.dto.ratingdto.RatingDTO;
import com.group.collectionofrecipes.entities.Rating;
import com.group.collectionofrecipes.entities.Recipe;
import com.group.collectionofrecipes.entities.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RatingMapper {

    public Rating toRatingEntity(CreateRatingDTO ratingDTO, User user, Recipe recipe) {
        return Rating.builder()
                .estimation(ratingDTO.getEstimation())
                .recipe(recipe)
                .user(user)
                .build();
    }

    public RatingDTO toRatingDto(Rating rating) {

        return RatingDTO.builder()
                .id(rating.getId())
                .recipeId(rating.getRecipe().getId())
                .userId(rating.getUser().getId())
                .build();
    }
}
