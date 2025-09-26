package com.group.collectionOfRecipes.mappers;

import com.group.collectionOfRecipes.dto.recipeDTO.CreateRecipeDTO;
import com.group.collectionOfRecipes.dto.recipeDTO.RecipeDTO;
import com.group.collectionOfRecipes.entities.Category;
import com.group.collectionOfRecipes.entities.Recipe;
import com.group.collectionOfRecipes.entities.User;
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
                .authorId(recipe.getAuthor().getId())
                .categoryId(recipe.getCategory().getId())
                .createdAt(recipe.getCreatedAt())
                .authorUsername(recipe.getAuthor().getUsername())
                .countOfServings(recipe.getCountOfServings())
                .categoryName(recipe.getCategory().getName())
                .likesCount(recipe.getFavoriteBy().size())
                .commentsCount(recipe.getComments().size())
                .averageRating(recipe.getTotalRating())
                .countOfRatings(recipe.getCountOfRatings())
                .build();
    }


    public Recipe toRecipeEntity(CreateRecipeDTO recipeDTO, User author, Category category) {
        return Recipe.builder()
                .title(recipeDTO.getTitle())
                .description(recipeDTO.getDescription())
                .instruction(recipeDTO.getInstruction())
                .cookingTime(recipeDTO.getCookingTime())
                .countOfServings(recipeDTO.getCountOfServings())
                .author(author)
                .category(category)
                .totalRating(0)
                .countOfRatings(0L)
                .build();
    }
}