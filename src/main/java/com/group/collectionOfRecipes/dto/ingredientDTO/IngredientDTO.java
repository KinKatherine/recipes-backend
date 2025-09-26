package com.group.collectionOfRecipes.dto.ingredientDTO;

import com.group.collectionOfRecipes.enums.Unit;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class IngredientDTO {

    private Long id;
    private String name;
    private Unit unit;
}
