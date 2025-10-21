package com.group.collectionofrecipes.dto.ratingdto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateRatingDTO {

    @NotNull()
    private Long recipeId;
    @NotNull()
    @Positive()
    @Min(1)
    @Max(5)
    private Integer estimation;
}
