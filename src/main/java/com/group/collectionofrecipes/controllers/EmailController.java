package com.group.collectionofrecipes.controllers;

import com.group.collectionofrecipes.services.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@Tag(name = "Email Verification")
@RestController
@RequiredArgsConstructor
@Slf4j
public class EmailController {

    private final UserService userService;

    @GetMapping("/api/v1/verify")
    public ResponseEntity<Void> verifyEmail(@RequestParam String token) {
        log.info("Get /api/v1/verify с токеном: {}", token);
        try {
            userService.verifyEmail(token);
            log.info("Email успешно подтвержден для токена: {}", token);
            return ResponseEntity.status(HttpStatus.FOUND)
                    .location(URI.create("https://recipes.poma.dev/"))
                    .build();
        } catch (Exception e) {
            log.error("Ошибка верификации для токена: {}. Причина: {}", token, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
}