package com.group.collectionOfRecipes.controllers;

import com.group.collectionOfRecipes.dto.recipeDTO.CreateRecipeDTO;
import com.group.collectionOfRecipes.dto.recipeDTO.RecipeDTO;
import com.group.collectionOfRecipes.services.RecipeService;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@Tag(name = "recipes_methods")
@RestController
@RequiredArgsConstructor
public class RecipeController {

    private final RecipeService recipeService;

    @GetMapping("/api/v1/recipes")
    public ResponseEntity<Map<String, Object>> getAllRecipes(@RequestParam(name = "title", required = false) String title) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("recipes", recipeService.findAllRecipes(title));
        return ResponseEntity.ok(response);
    }

    @GetMapping("/api/v1/recipes/{id}")
    public ResponseEntity<Map<String, Object>> getRecipeById(@PathVariable Long id) {

        Map<String, Object> response = new HashMap<>();
        RecipeDTO productDTO = recipeService.findRecipeById(id);
        response.put("status", "success");
        response.put("recipe", productDTO);
        return ResponseEntity.ok(response);
    }



    ///todo сделать чтобы передавался сам пользователь и записывался в рецепт его id
    /// userData = Principal principal.getName

    @PostMapping("/api/v1/recipes/create")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                    schema = @Schema(implementation = CreateRecipeDTO.class))
    )
    public ResponseEntity<Map<String, Object>> createRecipe(@RequestPart("recipe") @Valid CreateRecipeDTO createRecipeDTO,
                                                            @RequestPart("image") MultipartFile image) {
        Map<String, Object> response = new HashMap<>();
        RecipeDTO recipeDTO = recipeService.saveRecipe(createRecipeDTO, image);
        response.put("status", "success");
        response.put("message", "Рецепт c id " + recipeDTO.getId() + " успешно создан");
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/api/v1/recipes/delete/{id}")
    public ResponseEntity<Map<String, Object>> deleteRecipe(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();

        RecipeDTO recipeDTO = recipeService.deleteRecipe(id);
        response.put("status", "success");
        response.put("message", "Рецепт с ID " + recipeDTO.getId() + " удален");
        return ResponseEntity.ok(response);
    }


    @PutMapping("/api/v1/recipes/update/{id}")
    public ResponseEntity<Map<String, Object>> updateRecipe(@PathVariable Long id,
                                                            @RequestBody @Valid CreateRecipeDTO createRecipeDTO) {
        RecipeDTO updatedRecipe = recipeService.updateRecipe(id, createRecipeDTO);

        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Рецепт с ID " + updatedRecipe.getId() + " успешно обновлен");
        response.put("recipe", updatedRecipe);
        return ResponseEntity.ok(response);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleEntityNotFound(EntityNotFoundException e) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "error");
        response.put("message", e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }
}
