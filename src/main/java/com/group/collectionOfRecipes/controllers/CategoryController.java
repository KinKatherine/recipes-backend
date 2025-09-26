package com.group.collectionOfRecipes.controllers;

import com.group.collectionOfRecipes.dto.categoryDTO.CategoryDTO;
import com.group.collectionOfRecipes.dto.categoryDTO.CreateCategoryDTO;
import com.group.collectionOfRecipes.dto.recipeDTO.RecipeDTO;
import com.group.collectionOfRecipes.services.CategoryService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Tag(name = "categories_methods")
@RestController
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping("/api/v1/categories")
    public ResponseEntity<Map<String, Object>> getAllCategories(){

        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("categories", categoryService.findAllCategories());

        return ResponseEntity.ok(response);
    }


    @GetMapping("/api/v1/categories/{id}")
    public ResponseEntity<Map<String, Object>> getCategoryById(@PathVariable Long id){

        Map<String, Object> response = new HashMap<>();
        CategoryDTO categoryDTO = categoryService.findCategoryById(id);
        response.put("status", "success");
        response.put("category", categoryDTO);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/api/v1/categories/create") //только админ может это делать
    public ResponseEntity<Map<String, Object>> createCategory(@RequestBody @Valid CreateCategoryDTO createCategoryDTO) {
        Map<String, Object> response = new HashMap<>();
        CategoryDTO categoryDTO = categoryService.saveCategory(createCategoryDTO);
        response.put("status", "success");
        response.put("message", "Категория c id " + categoryDTO.getId() + " успешно создана");
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/api/v1/categories/delete/{id}") //только админ может это делать
                                                     // можно удалить только если у категории нет рецептов
    public ResponseEntity<Map<String, Object>> deleteCategory(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();

        try {
            CategoryDTO categoryDTO = categoryService.deleteCategory(id);
            response.put("status", "success");
            response.put("message", "Категория с ID " + categoryDTO.getId() + " удалена");
            return ResponseEntity.status(HttpStatus.OK).body(response);
        }
        catch (IllegalStateException e)
        {
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    @PutMapping("/api/v1/categories/update/{id}")
    public ResponseEntity<Map<String, Object>> updateCategory(@PathVariable Long id,
                                                            @RequestBody @Valid CreateCategoryDTO createCategoryDTO) {
        CategoryDTO updatedCategory = categoryService.updateCategory(id, createCategoryDTO);

        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Категория с ID " + updatedCategory.getId() + " успешно обновлена");
        response.put("category", updatedCategory);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/api/v1/category/{id}/recipes")
    public ResponseEntity<Map<String,Object>> getCategoryRecipes(@PathVariable Long id){
        Map<String,Object> response = new HashMap<>();
        List<RecipeDTO> recipeDTOList =categoryService.getRecipesByCategoryId(id);
        response.put("status", "success");
        response.put("category recipes", recipeDTOList);
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
