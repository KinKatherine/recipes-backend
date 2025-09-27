package com.group.collectionOfRecipes.dto.userDTO;

import com.group.collectionOfRecipes.enums.UserRole;
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

    @NotBlank
    private String password;
    @NotBlank

    private String confirmPassword;

    @NotBlank()
    @Email
    private String email;

    //private UserRole role;    // сами настраиваем роль пользователя при создании - USER

    /*@NotBlank()
    private String photo;*/
}
