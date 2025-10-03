package com.group.collectionofrecipes.dto.recipedto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateRecipeDTO {
    //это для Post - запросов

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

    @NotNull()
    private Long categoryId;
}
