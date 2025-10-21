package com.group.collectionofrecipes.dto.commentdto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateCommentDTO {

    @NotNull()
    private Long recipeId;

    @NotBlank()
    @Size(max = 1000)
    private String commentText;
}
