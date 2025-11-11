package com.group.collectionofrecipes.controllers;

import com.group.collectionofrecipes.dto.ingredientdto.IngredientDTO;
import com.group.collectionofrecipes.dto.recipedto.RecipeDTO;
import com.group.collectionofrecipes.dto.responsedto.ApiResponse;
import com.group.collectionofrecipes.entities.Ingredient;
import com.group.collectionofrecipes.enums.Unit;
import com.group.collectionofrecipes.services.IngredientService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

@Tag(name = "Ingredients")
@Slf4j
@RestController
@RequiredArgsConstructor
public class IngredientController {

    private final IngredientService ingredientService;

    @GetMapping("/api/v1/measures")
    public ApiResponse<List<String>> getStringUnits() {
        List<String> stringUnits = Arrays.stream(Unit.values()).map(Unit::getLabel).toList();
        return ApiResponse.success(stringUnits);
    }

    @GetMapping("/api/v1/ingregients")
    public ApiResponse<List<IngredientDTO>> getAllIngredients(@RequestParam(name = "name", required = false) String name) {
        log.info("GET /api/v1/ingregients");
        List<IngredientDTO> ingredients = ingredientService.findAllIngredients(name);
        log.info("GET /api/v1/ingregients - {} ингредиенты успешно получены", ingredients.size());
        return ApiResponse.success(ingredients);
    }
}
