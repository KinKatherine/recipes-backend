package com.group.collectionOfRecipes.dto.userDTO;

import lombok.Data;

@Data
public class JwtRequest {
    private String username;
    private String password;
}
