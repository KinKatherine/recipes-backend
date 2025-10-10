package com.group.collectionofrecipes.controllers;

import com.group.collectionofrecipes.dto.userdto.JwtRequest;
import com.group.collectionofrecipes.dto.userdto.JwtResponse;
import com.group.collectionofrecipes.dto.userdto.RegistrationUserDTO;
import com.group.collectionofrecipes.dto.userdto.UserDTO;
import com.group.collectionofrecipes.exceptions.AppError;
import com.group.collectionofrecipes.services.UserService;
import com.group.collectionofrecipes.utils.JwtTokenUtils;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

import static com.group.collectionofrecipes.utils.ApiConstants.FIELD_MESSAGE;
import static com.group.collectionofrecipes.utils.ApiConstants.FIELD_STATUS;
import static com.group.collectionofrecipes.utils.ApiConstants.FIELD_SUCCESS;

@Tag(name = "authentication_methods")
@RestController
@RequiredArgsConstructor
public class AuthUserController {
    private final UserService userService;
    private final JwtTokenUtils jwtTokenUtils;
    private final AuthenticationManager authenticationManager;

    @PostMapping("/api/v1/auth/login") //узнавание пользователя логин и пароль
    public ResponseEntity<Object> createAuthToken(@RequestBody JwtRequest authRequest) {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(authRequest.getUsername(), authRequest.getPassword()));

        } catch (BadCredentialsException e) {
            return new ResponseEntity<>(new AppError(HttpStatus.UNAUTHORIZED.value(), "Неправильный логин или пароль"), HttpStatus.UNAUTHORIZED);
        }

        UserDetails userDetails = userService.loadUserByUsername(authRequest.getUsername());
        String token = jwtTokenUtils.generateToken(userDetails);
        return ResponseEntity.ok(new JwtResponse(token));
    }

    @PostMapping("/api/v1/auth/register")
    public ResponseEntity<Object> createNewUser(@RequestBody RegistrationUserDTO registrationUserDTO) {
        if (!registrationUserDTO.getPassword().equals(registrationUserDTO.getConfirmPassword())) {
            return new ResponseEntity<>(new AppError(HttpStatus.BAD_REQUEST.value(), "Пароли не совпадают"), HttpStatus.BAD_REQUEST);
        }

        if (userService.findByUsername(registrationUserDTO.getUsername()).isPresent()) { //
            return new ResponseEntity<>(new AppError(HttpStatus.BAD_REQUEST.value(), "Пользователь с таким именем уже существует"), HttpStatus.BAD_REQUEST);
        }

        if (userService.findByUserEmail(registrationUserDTO.getEmail()).isPresent()) { //
            return new ResponseEntity<>(new AppError(HttpStatus.BAD_REQUEST.value(), "Пользователь с такой почтой уже существует"), HttpStatus.BAD_REQUEST);
        }
        Map<String, Object> response = new HashMap<>();
        UserDTO userDTO = userService.saveUser(registrationUserDTO);
        response.put(FIELD_STATUS, FIELD_SUCCESS);
        response.put(FIELD_MESSAGE, "Пользователь c id " + userDTO.getId() + " успешно создан");
        return ResponseEntity.ok(response);
    }
}
