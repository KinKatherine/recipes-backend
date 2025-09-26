package com.group.collectionOfRecipes.dto.userDTO;

import com.group.collectionOfRecipes.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDTO {

    private Long id;
    private String username;
    private String email;
    private UserRole role;
    private String photo;
    private Integer countLikeRecipes; // количество избранных рецептов
}
