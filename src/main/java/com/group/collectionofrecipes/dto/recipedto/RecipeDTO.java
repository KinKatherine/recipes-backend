package com.group.collectionofrecipes.dto.recipedto;

import com.group.collectionofrecipes.dto.commentdto.CommentDTO;
import com.group.collectionofrecipes.dto.ingredientdto.IngredientDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RecipeDTO {

    private Long id;
    private String title;
    private String description;
    private String instruction;
    private Integer cookingTime;
    private String image;
    private Integer countOfServings;
    private Integer averageRating;
    private Long authorId;
    private String authorUsername;
    private Long categoryId;
    private String categoryName;
    private Integer commentsCount;
    private LocalDateTime createdAt;
    private List<CommentDTO> commentDTOs;
    private List<IngredientDTO> ingredientDTOs;
    private Boolean isFavourite;
    private Boolean isAppreciated;
}
