package com.group.collectionofrecipes.dto.userdto;

import lombok.Data;

@Data
public class JwtRequest {
    private String username;
    private String password;
}
