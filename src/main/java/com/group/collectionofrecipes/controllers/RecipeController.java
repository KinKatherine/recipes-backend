package com.group.collectionofrecipes.controllers;

import com.group.collectionofrecipes.dto.recipedto.CreateRecipeDTO;
import com.group.collectionofrecipes.dto.recipedto.RecipeDTO;
import com.group.collectionofrecipes.services.RecipeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

import static com.group.collectionofrecipes.utils.ApiConstants.FIELD_ERROR;
import static com.group.collectionofrecipes.utils.ApiConstants.FIELD_MESSAGE;
import static com.group.collectionofrecipes.utils.ApiConstants.FIELD_STATUS;
import static com.group.collectionofrecipes.utils.ApiConstants.FIELD_SUCCESS;


@Tag(name = "recipes_methods")
@Slf4j
@RestController
@RequiredArgsConstructor
public class RecipeController {

    private final RecipeService recipeService;


    @Operation(
            summary = "Возвращает все рецепты / по названию",
            description = "Возвращает список всех рецептов. Если указан параметр 'title', возвращает рецепты, соответствующие названию."
    )
    @GetMapping("/api/v1/recipes")
    public ResponseEntity<Map<String, Object>> getAllRecipes(@RequestParam(name = "title", required = false) String title) {
        log.info("GET /api/v1/recipes");
        Map<String, Object> response = new HashMap<>();

        response.put(FIELD_STATUS, FIELD_SUCCESS);
        response.put("recipes", recipeService.findAllRecipes(title));
        return ResponseEntity.ok(response);
    }

    @GetMapping("/api/v1/recipes/{id}")
    public ResponseEntity<Map<String, Object>> getRecipeById(@PathVariable Long id) {

        Map<String, Object> response = new HashMap<>();
        RecipeDTO productDTO = recipeService.findRecipeById(id);
        response.put(FIELD_STATUS, FIELD_SUCCESS);
        response.put("recipe", productDTO);
        return ResponseEntity.ok(response);
    }


    @PostMapping("/api/v1/recipes")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                    schema = @Schema(implementation = CreateRecipeDTO.class))
    )
    //всё таки создаём без подтверждения?
    public ResponseEntity<Map<String, Object>> createRecipe(@RequestPart("recipe") @Valid CreateRecipeDTO createRecipeDTO,
                                                            @RequestPart("image") MultipartFile image,
                                                            Principal principal) {
        Map<String, Object> response = new HashMap<>();
        RecipeDTO recipeDTO = recipeService.saveRecipe(createRecipeDTO, image, principal);
        response.put(FIELD_STATUS, FIELD_SUCCESS);
        response.put(FIELD_MESSAGE, "Рецепт c id " + recipeDTO.getId() + " успешно создан");
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/api/v1/recipes/{id}")
    public ResponseEntity<Map<String, Object>> deleteRecipe(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();

        RecipeDTO recipeDTO = recipeService.deleteRecipe(id);
        response.put(FIELD_STATUS, FIELD_SUCCESS);
        response.put(FIELD_MESSAGE, "Рецепт с ID " + recipeDTO.getId() + " удален");
        return ResponseEntity.ok(response);
    }


    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/api/v1/recipes/{id}")
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
        response.put(FIELD_STATUS, FIELD_ERROR);
        response.put(FIELD_MESSAGE, e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }
}
