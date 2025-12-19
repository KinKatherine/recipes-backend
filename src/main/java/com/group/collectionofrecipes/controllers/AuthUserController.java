package com.group.collectionofrecipes.controllers;

import com.group.collectionofrecipes.dto.responsedto.ApiResponse;
import com.group.collectionofrecipes.dto.userdto.JwtRequest;
import com.group.collectionofrecipes.dto.userdto.JwtResponse;
import com.group.collectionofrecipes.dto.userdto.LanguageRequest;
import com.group.collectionofrecipes.dto.userdto.RegistrationUserDTO;
import com.group.collectionofrecipes.dto.userdto.UserDTO;
import com.group.collectionofrecipes.exceptions.AppError;
import com.group.collectionofrecipes.exceptions.InvalidUserInfoException;
import com.group.collectionofrecipes.exceptions.UnauthorizedUserException;
import com.group.collectionofrecipes.exceptions.UnsupportedLanguageException;
import com.group.collectionofrecipes.services.UserService;
import com.group.collectionofrecipes.utils.JwtTokenUtils;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.security.Principal;
import java.util.List;
import java.util.Map;

import static com.group.collectionofrecipes.utils.ApiConstants.FIELD_ERROR;
import static com.group.collectionofrecipes.utils.ApiConstants.FIELD_MESSAGE;
import static com.group.collectionofrecipes.utils.ApiConstants.FIELD_STATUS;
import static com.group.collectionofrecipes.utils.ApiConstants.FIELD_SUCCESS;

@Tag(name = "Authentications")
@Slf4j
@RestController
@RequiredArgsConstructor
public class AuthUserController {
    private final UserService userService;
    private final JwtTokenUtils jwtTokenUtils;
    private final AuthenticationManager authenticationManager;

    @PostMapping("/api/v1/auth/login")
    public ResponseEntity<Object> createAuthToken(@RequestBody @Valid JwtRequest authRequest) {
        log.info("Post  /api/v1/auth/login");
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(authRequest.getUsername(), authRequest.getPassword()));
        } catch (BadCredentialsException e) {
            log.error("Ошибка аутентификации для пользователя: {}", authRequest.getUsername());
            return new ResponseEntity<>(new AppError(HttpStatus.UNAUTHORIZED.value(), "Неправильный логин или пароль"), HttpStatus.UNAUTHORIZED);
        }

        UserDetails userDetails = userService.loadUserByUsername(authRequest.getUsername());
        String token = jwtTokenUtils.generateToken(userDetails);
        log.info("Успешный вход пользователя: {}", authRequest.getUsername());
        return ResponseEntity.ok(new JwtResponse(token));
    }

    @PostMapping("/api/v1/auth/register")
    public ResponseEntity<Object> createNewUser(@RequestBody @Valid RegistrationUserDTO registrationUserDTO) {
        log.info("Post  /api/v1/auth/register");
        try {
            if (!registrationUserDTO.getPassword().equals(registrationUserDTO.getConfirmPassword())) {
                log.error("Пароли не совпадают для пользователя: {}", registrationUserDTO.getUsername());
                return ResponseEntity.badRequest()
                        .body(new AppError(HttpStatus.BAD_REQUEST.value(), "Пароли не совпадают"));
            }

            if (!userService.isUsernameAvailable(registrationUserDTO.getUsername())) {
                log.error("Пользователь с таким именем уже существует: {}", registrationUserDTO.getUsername());
                return ResponseEntity.badRequest()
                        .body(new AppError(HttpStatus.BAD_REQUEST.value(), "Пользователь с таким именем уже существует"));
            }

            if (!userService.isEmailAvailable(registrationUserDTO.getEmail())) {
                log.error("Пользователь с такой почтой уже существует: {}", registrationUserDTO.getEmail());
                return ResponseEntity.badRequest()
                        .body(new AppError(HttpStatus.BAD_REQUEST.value(), "Пользователь с такой почтой уже существует"));
            }

            UserDTO userDTO = userService.saveUser(registrationUserDTO);
            log.info("Пользователь успешно создан: {}", registrationUserDTO.getUsername());

            Map<String, Object> response = Map.of(
                    FIELD_STATUS, FIELD_SUCCESS,
                    FIELD_MESSAGE, "Пользователь c id " + userDTO.getId() + " успешно создан. Проверьте почту для подтверждения."
            );
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Ошибка при создании пользователя: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new AppError(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Ошибка при создании пользователя" + e.getMessage()));
        }
    }

    @PostMapping("/api/v1/avatars")
    public ApiResponse<String> createUserAvatar(@RequestPart("image") MultipartFile image,
                                                Principal principal) {
        log.info("Post  /api/v1/avatars");
        userService.createUserAvatar(image, principal);
        log.info("Аватарка пользователя {} успешно создана", principal.getName());
        return ApiResponse.success();
    }

    @DeleteMapping("/api/v1/avatars")
    public ApiResponse<String> deleteUserAvatar(Principal principal) {
        log.info("Post  /api/v1/avatars");
        userService.deleteUserAvatar(principal);
        log.info("Аватарка пользователя {} успешно удалена", principal.getName());
        return ApiResponse.success();
    }

    @PostMapping("/api/v1/users/language")
    public ApiResponse<String> updateUserLanguage(@RequestBody @Valid LanguageRequest language , Principal principal) {
        log.info("Post  /api/v1/users/language");
        userService.updateUserLanguage(principal, language);
        log.info("Язык пользователя {} успешно изменен", principal.getName());
        return ApiResponse.success();
    }

    @GetMapping("/api/v1/users/language")
    public ApiResponse<String> getUserLanguage(Principal principal) {
        log.info("Get  /api/v1/users/language");
        String language = userService.getUserLanguage(principal);
        log.info("Язык пользователя {} успешно получен", principal.getName());
        return ApiResponse.success(language);
    }


    @GetMapping("/api/v1/language")
    public ResponseEntity<ApiResponse<Void>> setGuestLanguage(@RequestParam("lang") String lang,
                                                              HttpServletResponse response) {
        log.info("Get /api/v1/language - Установка куки для гостя: {}", lang);
        List<String> supportedLanguages = List.of("ru", "en");
        String languageCode = lang.toLowerCase();
        if (!supportedLanguages.contains(languageCode)) {
            throw new UnsupportedLanguageException("Language not supported: " + languageCode);
        }
        Cookie cookie = new Cookie("app_lang", languageCode);
        cookie.setPath("/");
        cookie.setMaxAge(60 * 60 * 24 * 30);
        response.addCookie(cookie);
        return ResponseEntity.ok(ApiResponse.success());
    }

    @ExceptionHandler(UnsupportedLanguageException.class)
    public ResponseEntity<Map<String, Object>> handleUnsupportedLanguage(UnsupportedLanguageException e) {
        log.warn("Обработка исключения UnsupportedLanguageException: {} ", e.getMessage());
        Map<String, Object> response = new HashMap<>();
        response.put(FIELD_STATUS, FIELD_ERROR);
        response.put(FIELD_MESSAGE, e.getMessage());
        log.warn("Возврат ответа 400 BAD_REQUEST: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(UnauthorizedUserException.class)
    public ResponseEntity<Map<String, Object>> handleUnauthorizedUser(UnauthorizedUserException e) {
        log.warn("Обработка исключения UnauthorizedUserException: {} ", e.getMessage());
        Map<String, Object> response = new HashMap<>();
        response.put(FIELD_STATUS, FIELD_ERROR);
        response.put(FIELD_MESSAGE, e.getMessage());
        log.warn("Возврат ответа 401 UNAUTHORIZED: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException e) {
        log.warn("Обработка исключения IllegalArgumentException: {} ", e.getMessage());
        Map<String, Object> response = new HashMap<>();
        response.put(FIELD_STATUS, FIELD_ERROR);
        response.put(FIELD_MESSAGE, e.getMessage());
        log.warn("Возврат ответа 400 BAD_REQUEST: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleIEntityNotFound(EntityNotFoundException e) {
        log.warn("Обработка исключения EntityNotFoundException: {} ", e.getMessage());
        Map<String, Object> response = new HashMap<>();
        response.put(FIELD_STATUS, FIELD_ERROR);
        response.put(FIELD_MESSAGE, e.getMessage());
        log.warn("Возврат ответа 404 Not found: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }


    @GetMapping("/api/v1/users/avatar")
    public ApiResponse<String> findUserAvatar(Principal principal) {
        String avatar = userService.getUserAvatar(principal);
        return ApiResponse.success(avatar);
    }


    @GetMapping("/api/v1/validation/check-username")
    public ApiResponse<Boolean> checkUsername(@RequestParam String username) {
        boolean isAvailable = userService.isUsernameAvailable(username);
        return ApiResponse.success(isAvailable);
    }

    @GetMapping("/api/v1/validation/check-email")
    public ApiResponse<Boolean> checkEmail(@RequestParam String email) {
        boolean isAvailable = userService.isEmailAvailable(email);
        return ApiResponse.success(isAvailable);
    }

    @ExceptionHandler(InvalidUserInfoException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidUsername(InvalidUserInfoException e) {
        log.warn("Обработка исключения InvalidUsernameException: {} ", e.getMessage());

        Map<String, Object> response = new HashMap<>();
        response.put(FIELD_STATUS, FIELD_ERROR);
        response.put(FIELD_MESSAGE, e.getMessage());
        log.warn("Возврат ответа 400 : {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
}