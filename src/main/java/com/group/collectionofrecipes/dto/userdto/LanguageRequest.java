package com.group.collectionofrecipes.dto.userdto;

import com.group.collectionofrecipes.enums.Language;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LanguageRequest {
    @NotNull
    private Language language;
}