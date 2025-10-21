package com.group.collectionofrecipes.controllers;

import com.group.collectionofrecipes.dto.recipedto.CreateRecipeDTO;
import com.group.collectionofrecipes.dto.recipedto.RecipeDTO;
import com.group.collectionofrecipes.services.RecipeService;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.group.collectionofrecipes.utils.ApiConstants.FIELD_ERROR;
import static com.group.collectionofrecipes.utils.ApiConstants.FIELD_MESSAGE;
import static com.group.collectionofrecipes.utils.ApiConstants.FIELD_RECIPE;
import static com.group.collectionofrecipes.utils.ApiConstants.FIELD_STATUS;
import static com.group.collectionofrecipes.utils.ApiConstants.FIELD_SUCCESS;


@Tag(name = "Recipes")
@Slf4j
@RestController
@RequiredArgsConstructor
public class RecipeController {

    private final RecipeService recipeService;


    //ГЛАВНАЯ СТРАНИЦА
    @GetMapping("/api/v1/recipes/recipe-of-the-day")
    public ResponseEntity<Map<String, Object>> getRecipeOfTheDay() {
        Map<String, Object> response = new HashMap<>();
        RecipeDTO recipeDTO = recipeService.getRecipeOfTheDay();
        response.put(FIELD_STATUS, FIELD_SUCCESS);
        response.put(FIELD_RECIPE, recipeDTO);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/api/v1/recipes/recent")
    public ResponseEntity<Map<String,Object>> getRecentRecipes(Principal principal) {
        Map<String, Object> response = new HashMap<>();
        List<RecipeDTO> recentRecipes = recipeService.getLast3AddedRecipes(principal);
        response.put(FIELD_STATUS, FIELD_SUCCESS);
        response.put(FIELD_RECIPE, recentRecipes);
        return ResponseEntity.ok(response);
    }

    //СТРАНИЦА РЕЦЕПТА
    @GetMapping("/api/v1/recipes/{id}")
    public ResponseEntity<Map<String, Object>> getRecipeById(@PathVariable Long id,Principal principal) {
        Map<String, Object> response = new HashMap<>();
        RecipeDTO recipeDTO = recipeService.getRecipeById(id,principal);
        response.put(FIELD_STATUS, FIELD_SUCCESS);
        response.put(FIELD_RECIPE, recipeDTO);
        return ResponseEntity.ok(response);
    }

    //СТРАНИЦА ПОЛЬЗОВАТЕЛЯ ИЗБРАННОЕ
    @GetMapping("/api/v1/recipes/favourites")
    public ResponseEntity<Map<String,Object>> getUserFavouriteRecipes(@RequestParam(defaultValue = "0") int page, Principal principal) {
        Page<RecipeDTO> recipePage = recipeService.getFavouriteUserRecipes(principal,page);
        return ResponseEntity.ok(getRecipeResponse(recipePage));
    }

    //СТРАНИЦА ПОЛЬЗОВАТЕЛЯ МОИ ПОДТВЕРЖДЕННЫЕ РЕЦЕПТЫ
    @GetMapping("/api/v1/recipes/my-recipes")
    public ResponseEntity<Map<String,Object>> getUserAddedConfirmedRecipes(@RequestParam(defaultValue = "0") int page,Principal principal) {
        Page<RecipeDTO> recipePage = recipeService.getUserAddedConfirmedRecipes(principal,page);
        return ResponseEntity.ok(getRecipeResponse(recipePage));
    }

    //СТРАНИЦА АДМИНИСТРАТОРА НЕПОДТВЕРЖДЕННЫЕ РЕЦЕПТЫ
    @GetMapping("/api/v1/recipes/unconfirmed")
    public ResponseEntity<Map<String,Object>> getUnconfirmedRecipes(@RequestParam(defaultValue = "0") int page) {
        Page<RecipeDTO> recipePage = recipeService.getUnconfirmedRecipes(page);
        return ResponseEntity.ok(getRecipeResponse(recipePage));
    }

    //ОДОБРЕНИЕ РЕЦЕПТА АДМИНОМ
    // добавить сохранение ингредиента
    @GetMapping("/api/v1/recipes/confirm")
    public ResponseEntity<Map<String,Object>> confirmRecipe(@RequestParam Long id){
        Map<String,Object> response = new HashMap<>();
        recipeService.confirmRecipe(id);
        response.put(FIELD_STATUS, FIELD_SUCCESS);
        return ResponseEntity.ok(response);
    }


    //УТИЛЬНЫЙ МЕТОД
    private Map<String,Object> getRecipeResponse(Page<RecipeDTO> recipePage){
        Map<String, Object> response = new HashMap<>();
        response.put(FIELD_STATUS, FIELD_SUCCESS);
        response.put("recipes", recipePage.getContent());
        response.put("currentPage", recipePage.getNumber());
        response.put("totalPages", recipePage.getTotalPages());
        response.put("totalItems", recipePage.getTotalElements());
        response.put("pageSize", recipePage.getSize());
        response.put("hasNext", recipePage.hasNext());
        response.put("hasPrevious", recipePage.hasPrevious());
        return response;
    }

    @PostMapping("/api/v1/recipes")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                    schema = @Schema(implementation = CreateRecipeDTO.class))
    )
    public ResponseEntity<Map<String, Object>> createRecipe(@RequestPart("recipe") @Valid CreateRecipeDTO createRecipeDTO,
                                                            @RequestPart("image") MultipartFile image,
                                                            Principal principal) {
        Map<String, Object> response = new HashMap<>();
        RecipeDTO recipeDTO = recipeService.saveRecipe(createRecipeDTO, image, principal);
        response.put(FIELD_STATUS, FIELD_SUCCESS);
        response.put(FIELD_MESSAGE, "Рецепт c id " + recipeDTO.getId() + " успешно создан");
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
