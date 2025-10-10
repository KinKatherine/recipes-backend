package com.group.collectionofrecipes.dto.userdto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegistrationUserDTO {

    @NotBlank()
    private String username;

    @NotBlank()
    private String password;

    @NotBlank()
    private String confirmPassword;

    @NotBlank()
    @Email
    private String email;
}
