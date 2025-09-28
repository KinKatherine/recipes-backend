package com.group.collectionOfRecipes.controllers;

import com.group.collectionOfRecipes.dto.categoryDTO.CategoryDTO;
import com.group.collectionOfRecipes.dto.categoryDTO.CreateCategoryDTO;
import com.group.collectionOfRecipes.dto.recipeDTO.RecipeDTO;
import com.group.collectionOfRecipes.services.CategoryService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Tag(name = "categories_methods")
@RestController
@RequiredArgsConstructor
@Slf4j
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping("/api/v1/categories")
    public ResponseEntity<Map<String, Object>> getAllCategories(){
        log.info("GET /api/v1/categories");

        Map<String, Object> response = new HashMap<>();
        List<CategoryDTO> categories = categoryService.findAllCategories();

        response.put("status", "success");
        response.put("categories", categories);
        log.info("GET /api/v1/categories - возвращено {} категорий", categories.size());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/api/v1/categories/{id}")
    public ResponseEntity<Map<String, Object>> getCategoryById(@PathVariable Long id){
        log.info(" GET /api/v1/categories/{}", id);

        Map<String, Object> response = new HashMap<>();
        CategoryDTO categoryDTO = categoryService.findCategoryById(id);

        response.put("status", "success");
        response.put("category", categoryDTO);
        log.info("GET /api/v1/categories/{} - Успешно возвращена категория: {}", id, categoryDTO.getName());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/api/v1/categories/create")
    public ResponseEntity<Map<String, Object>> createCategory(@RequestBody @Valid CreateCategoryDTO createCategoryDTO) {
        log.info("POST /api/v1/categories/create");

        Map<String, Object> response = new HashMap<>();
        CategoryDTO categoryDTO = categoryService.saveCategory(createCategoryDTO);

        response.put("status", "success");
        response.put("message", "Категория c id " + categoryDTO.getId() + " успешно создана");
        log.info("POST /api/v1/categories/create - Категория успешно создана: ID={}, Name={}",
                categoryDTO.getId(), categoryDTO.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/api/v1/categories/delete/{id}")
    public ResponseEntity<Map<String, Object>> deleteCategory(@PathVariable Long id) {
        log.info("DELETE /api/v1/categories/delete/{}", id);

        Map<String, Object> response = new HashMap<>();

        try {
            CategoryDTO categoryDTO = categoryService.deleteCategory(id);
            response.put("status", "success");
            response.put("message", "Категория с ID " + categoryDTO.getId() + " удалена");
            log.info("DELETE /api/v1/categories/delete/{} - Категория успешно удалена: {}",
                    id, categoryDTO.getName());
            return ResponseEntity.status(HttpStatus.OK).body(response);
        }
        catch (IllegalStateException e) {
            log.warn("DELETE /api/v1/categories/delete/{} - Ошибка удаления: {}", id, e.getMessage());

            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response); // Изменил на BAD_REQUEST
        }
    }

    @PutMapping("/api/v1/categories/update/{id}")
    public ResponseEntity<Map<String, Object>> updateCategory(@PathVariable Long id,
                                                              @RequestBody @Valid CreateCategoryDTO createCategoryDTO) {
        log.info("PUT /api/v1/categories/update/{}", id);
        log.debug("Новые данные: name={}, description={}",
                createCategoryDTO.getName(), createCategoryDTO.getDescription());

        CategoryDTO updatedCategory = categoryService.updateCategory(id, createCategoryDTO);

        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Категория с ID " + updatedCategory.getId() + " успешно обновлена");
        response.put("category", updatedCategory);
        log.info("PUT /api/v1/categories/update/{} - Категория успешно обновлена: {}",
                id, updatedCategory.getName());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/api/v1/category/{id}/recipes")
    public ResponseEntity<Map<String,Object>> getCategoryRecipes(@PathVariable Long id){
        log.info("GET /api/v1/category/{}/recipes", id);

        Map<String,Object> response = new HashMap<>();
        List<RecipeDTO> recipeDTOList = categoryService.getRecipesByCategoryId(id);

        response.put("status", "success");
        response.put("category recipes", recipeDTOList);
        log.info("GET /api/v1/category/{}/recipes - Успешно возвращено {} рецептов", id, recipeDTOList.size());
        return ResponseEntity.ok(response);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleEntityNotFound(EntityNotFoundException e) {
        log.error("Обработка исключения EntityNotFoundException: {} ", e.getMessage());

        Map<String, Object> response = new HashMap<>();
        response.put("status", "error");
        response.put("message", e.getMessage());
        log.warn("Возврат ответа 404 Not Found: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException e) {
        log.error("Обработка исключения IllegalArgumentException: {} ", e.getMessage());

        Map<String, Object> response = new HashMap<>();
        response.put("status", "error");
        response.put("message", e.getMessage());
        log.warn("Возврат ответа 400 Bad Request: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception e) {
        log.error("Обработка общего исключения: {} ", e.getMessage(), e);

        Map<String, Object> response = new HashMap<>();
        response.put("status", "error");
        response.put("message", "Внутренняя ошибка сервера");

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}