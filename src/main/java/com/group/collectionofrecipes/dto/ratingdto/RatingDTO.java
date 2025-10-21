package com.group.collectionofrecipes.dto.ratingdto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RatingDTO {
    private  Long id;
    private  Long userId;
    private  Long recipeId;
}
