package com.group.collectionofrecipes.dto.userdto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
@AllArgsConstructor
public class JwtRequest {

    @NotBlank()
    @Size(min = 3, max = 50)
    private String username;

    @NotBlank()
    @Size(min = 6)
    private String password;
}
