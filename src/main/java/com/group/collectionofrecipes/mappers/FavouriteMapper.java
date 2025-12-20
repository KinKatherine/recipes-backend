package com.group.collectionofrecipes.mappers;

import com.group.collectionofrecipes.dto.favouritedto.FavouriteDTO;
import com.group.collectionofrecipes.entities.Favourite;
import com.group.collectionofrecipes.entities.Recipe;
import com.group.collectionofrecipes.entities.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FavouriteMapper {

    public FavouriteDTO toFavouriteDto(Favourite favourite) {

        return FavouriteDTO.builder()
                .id(favourite.getId())
                .recipeId(favourite.getRecipe().getId())
                .authorId(favourite.getUser().getId())
                .authorUsername(favourite.getUser().getUsername())
                .build();
    }

    public Favourite toFavouriteEntity(Recipe recipe, User user) {

        return Favourite.builder()
                .user(user)
                .recipe(recipe)
                .build();
    }
}
