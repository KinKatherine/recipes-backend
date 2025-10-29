package com.group.collectionofrecipes.controllers;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@Tag(name = "Health")
@Slf4j
@RestController
@RequiredArgsConstructor
public class HealthController {

    @GetMapping("/health")
    public ResponseEntity<String> getRecentRecipes(Principal principal) {
        log.info("GET /health");
        return ResponseEntity.ok("OK");
    }
}
