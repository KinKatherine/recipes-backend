package com.group.collectionofrecipes.dto.categorydto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateCategoryDTO {

    @NotBlank()
    @Size(min = 3, max = 100)
    private String name;

    @NotBlank()
    @Size(min = 10, max = 1000)
    private String description;

}
