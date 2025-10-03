package com.group.collectionofrecipes.dto.ingredientdto;

import com.group.collectionofrecipes.enums.Unit;
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
