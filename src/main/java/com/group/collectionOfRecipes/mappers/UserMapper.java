package com.group.collectionOfRecipes.mappers;

import com.group.collectionOfRecipes.dto.userDTO.UserDTO;
import com.group.collectionOfRecipes.entities.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserDTO toUserDto(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setRole(user.getRole());
        dto.setPhoto(user.getPhoto());
        dto.setCountLikeRecipes(user.getFavourites().size());

        return dto;
    }

    public User toUserEntity(UserDTO userDTO) {
        return User.builder()
                .username(userDTO.getUsername())
                .email(userDTO.getEmail())
                .role(userDTO.getRole())
                .photo(userDTO.getPhoto())
                .build();
    }

}
