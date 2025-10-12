package com.group.collectionofrecipes.mappers;

import com.group.collectionofrecipes.dto.userdto.RegistrationUserDTO;
import com.group.collectionofrecipes.dto.userdto.UserDTO;
import com.group.collectionofrecipes.entities.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
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

    public User toUserEntity(RegistrationUserDTO userDTO) {
        return User.builder()
                .username(userDTO.getUsername())
                .email(userDTO.getEmail())
                .password(userDTO.getPassword())
                .build();
    }

}
