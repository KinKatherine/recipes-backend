package com.group.collectionOfRecipes.dto.recipeDTO;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateRecipeDTO {                  ///это для Post - запросов

    @NotBlank()
    @Size(min = 3, max = 100)
    private String title;

    @NotBlank()
    @Size(min = 10, max = 1000)
    private String description;

    @NotBlank()
    @Size(min = 10, max = 5000)
    private String instruction;

    @NotNull()
    @Positive()
    @Min(value = 1)
    @Max(value = 360)
    private Integer cookingTime;

    @NotNull()
    @Positive()
    @Min(value = 1)
    @Max(value = 20)
    private Integer countOfServings;

    private Long authorId;     //получу из Spring Security

    @NotNull()
    private Long categoryId;
}
