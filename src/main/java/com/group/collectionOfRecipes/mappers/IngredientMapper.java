package com.group.collectionOfRecipes.mappers;

import com.group.collectionOfRecipes.dto.ingredientDTO.IngredientDTO;
import com.group.collectionOfRecipes.entities.Ingredient;
import org.springframework.stereotype.Component;

@Component
public class IngredientMapper {

    public IngredientDTO toIngredientDto(Ingredient recipe) {
        IngredientDTO dto = new IngredientDTO();
        dto.setId(recipe.getId());
        dto.setName(recipe.getName());
        dto.setUnit(recipe.getUnit());

        return dto;
    }

    public Ingredient toIngredientEntity(IngredientDTO ingredientDTO) {
        return Ingredient.builder()
                .name(ingredientDTO.getName())
                .unit(ingredientDTO.getUnit())
                .build();
    }
}
