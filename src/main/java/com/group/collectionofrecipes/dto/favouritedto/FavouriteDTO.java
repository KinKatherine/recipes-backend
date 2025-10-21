package com.group.collectionofrecipes.dto.favouritedto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FavouriteDTO {

    private Long id;
    private Long authorId;
    private String authorUsername;
    private Long recipeId;
}
