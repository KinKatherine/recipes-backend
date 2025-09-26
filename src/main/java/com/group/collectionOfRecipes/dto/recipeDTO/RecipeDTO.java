package com.group.collectionOfRecipes.dto.recipeDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RecipeDTO {                /// это дто для Get - запросов

    private Long id;
    private String title;
    private String description;
    private String instruction;
    private Integer cookingTime;
    private String image;
    private Integer countOfServings; // количество порций

    private Integer averageRating;
    private Long countOfRatings;

    private Long authorId;
    private String authorUsername;
    private Long categoryId;
    private String categoryName;
    private Integer likesCount; // количество добавлений в избранное
    private Integer commentsCount; // количество комментариев
    private LocalDateTime createdAt;
}
