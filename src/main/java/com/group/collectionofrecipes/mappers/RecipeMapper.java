package com.group.collectionofrecipes.mappers;

import com.group.collectionofrecipes.dto.recipedto.CreateRecipeDTO;
import com.group.collectionofrecipes.dto.recipedto.RecipeDTO;
import com.group.collectionofrecipes.entities.Category;
import com.group.collectionofrecipes.entities.Recipe;
import com.group.collectionofrecipes.entities.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
public class RecipeMapper {

    public RecipeDTO toRecipeDto(Recipe recipe) {

         return RecipeDTO.builder()
                .id(recipe.getId())
                .title(recipe.getTitle())
                .description(recipe.getDescription())
                .instruction(recipe.getInstruction())
                .cookingTime(recipe.getCookingTime())
                .image(recipe.getImage())
                .countOfServings(recipe.getCountOfServings())
                .averageRating(0)
                .countOfRatings(0L)
                 .userRating(null)
                .authorId(recipe.getAuthor().getId())
                .authorUsername(recipe.getAuthor().getUsername())
                .categoryId(recipe.getCategory().getId())
                .categoryName(recipe.getCategory().getName())
                .createdAt(recipe.getCreatedAt())
                .commentsCount(null)
                .build();
    }


    public Recipe toRecipeEntity(CreateRecipeDTO recipeDTO, User author, Category category, String image) {
        return Recipe.builder()
                .title(recipeDTO.getTitle())
                .description(recipeDTO.getDescription())
                .instruction(recipeDTO.getInstruction())
                .cookingTime(recipeDTO.getCookingTime())
                .countOfServings(recipeDTO.getCountOfServings())
                .author(author)
                .category(category)
                .image(image)
                .build();
    }
}