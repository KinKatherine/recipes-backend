package com.group.collectionofrecipes.dto.ingredientdto;

import com.group.collectionofrecipes.enums.Unit;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateIngredientDTO {

    @NotBlank()
    @Size(min = 3, max = 50)
    private String name;

    @NotBlank()
    private Double amount;

    @NotBlank()
    private Unit unit;

    private Boolean isConfirmed;

}
