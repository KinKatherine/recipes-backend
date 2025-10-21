package com.group.collectionofrecipes.mappers;

import com.group.collectionofrecipes.dto.ingredientdto.CreateIngredientDTO;
import com.group.collectionofrecipes.dto.ingredientdto.IngredientDTO;
import com.group.collectionofrecipes.entities.Ingredient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class IngredientMapper {

    public IngredientDTO toIngredientDto(Ingredient ingredient) {
        IngredientDTO dto = new IngredientDTO();
        dto.setId(ingredient.getId());
        dto.setName(ingredient.getName());
        return dto;
    }

    public Ingredient toIngredientEntity(CreateIngredientDTO ingredientDTO) {
        return Ingredient.builder()
                .name(ingredientDTO.getName())
                .build();
    }
}
