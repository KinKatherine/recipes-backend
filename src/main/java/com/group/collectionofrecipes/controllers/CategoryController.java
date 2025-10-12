package com.group.collectionofrecipes.controllers;

import com.group.collectionofrecipes.dto.categorydto.CategoryDTO;
import com.group.collectionofrecipes.dto.categorydto.CreateCategoryDTO;
import com.group.collectionofrecipes.dto.recipedto.RecipeDTO;
import com.group.collectionofrecipes.services.CategoryService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.group.collectionofrecipes.utils.ApiConstants.FIELD_ERROR;
import static com.group.collectionofrecipes.utils.ApiConstants.FIELD_MESSAGE;
import static com.group.collectionofrecipes.utils.ApiConstants.FIELD_STATUS;
import static com.group.collectionofrecipes.utils.ApiConstants.FIELD_SUCCESS;


@Tag(name = "Categories")
@RestController
@RequiredArgsConstructor
@Slf4j
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping("/api/v1/categories")
    public ResponseEntity<Map<String, Object>> getAllCategories() {
        log.info("GET /api/v1/categories");

        Map<String, Object> response = new HashMap<>();
        List<CategoryDTO> categories = categoryService.findAllCategories();

        response.put(FIELD_STATUS, FIELD_SUCCESS);
        response.put("categories", categories);
        log.info("GET /api/v1/categories - возвращено {} категорий", categories.size());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/api/v1/categories/{id}") //наверное закрытый...или вообще не нужен
    public ResponseEntity<Map<String, Object>> getCategoryById(@PathVariable Long id) {
        log.info("GET /api/v1/categories/{}", id);

        Map<String, Object> response = new HashMap<>();
        CategoryDTO categoryDTO = categoryService.findCategoryById(id);

        response.put(FIELD_STATUS, FIELD_SUCCESS);
        response.put("category", categoryDTO);
        log.info("GET /api/v1/categories/{} - Успешно возвращена категория: {}", id, categoryDTO.getName());
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/api/v1/categories")
    public ResponseEntity<Map<String, Object>> createCategory(@RequestBody @Valid CreateCategoryDTO createCategoryDTO) {
        log.info("POST /api/v1/categories/admin/create");

        Map<String, Object> response = new HashMap<>();
        CategoryDTO categoryDTO = categoryService.saveCategory(createCategoryDTO);

        response.put(FIELD_STATUS, FIELD_SUCCESS);
        response.put(FIELD_MESSAGE, "Категория c id " + categoryDTO.getId() + " успешно создана");
        log.info("POST /api/v1/categories/admin/create - Категория успешно создана: ID={}, Name={}",
                categoryDTO.getId(), categoryDTO.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/api/v1/categories/{id}")
    public ResponseEntity<Map<String, Object>> deleteCategory(@PathVariable Long id) {
        log.info("DELETE /api/v1/categories/admin/delete/{}", id);

        Map<String, Object> response = new HashMap<>();

        CategoryDTO categoryDTO = categoryService.deleteCategory(id);
        response.put(FIELD_STATUS, FIELD_SUCCESS);
        response.put(FIELD_MESSAGE, "Категория с ID " + categoryDTO.getId() + " удалена");
        log.info("DELETE /api/v1/categories/admin/delete/{} - Категория успешно удалена: {}", id, categoryDTO.getName());
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/api/v1/categories/{id}")
    public ResponseEntity<Map<String, Object>> updateCategory(@PathVariable Long id,
                                                              @RequestBody @Valid CreateCategoryDTO createCategoryDTO) {
        log.info("PUT /api/v1/categories/admin/update/{}", id);
        log.debug("Новые данные: name={}, description={}",
                createCategoryDTO.getName(), createCategoryDTO.getDescription());

        CategoryDTO updatedCategory = categoryService.updateCategory(id, createCategoryDTO);

        Map<String, Object> response = new HashMap<>();
        response.put(FIELD_STATUS, FIELD_SUCCESS);
        response.put(FIELD_MESSAGE, "Категория с ID " + updatedCategory.getId() + " успешно обновлена");
        response.put("category", updatedCategory);
        log.info("PUT /api/v1/categories/admin/update/{} - Категория успешно обновлена: {}",
                id, updatedCategory.getName());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/api/v1/categories/{id}/recipes")
    public ResponseEntity<Map<String, Object>> getCategoryRecipes(@PathVariable Long id) {
        log.info("GET /api/v1/category/{}/recipes", id);

        Map<String, Object> response = new HashMap<>();
        List<RecipeDTO> recipeDTOList = categoryService.getRecipesByCategoryId(id);

        response.put(FIELD_STATUS, FIELD_SUCCESS);
        response.put("category recipes", recipeDTOList);
        log.info("GET /api/v1/category/{}/recipes - Успешно возвращено {} рецептов", id, recipeDTOList.size());
        return ResponseEntity.ok(response);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleEntityNotFound(EntityNotFoundException e) {
        log.error("Обработка исключения EntityNotFoundException: {} ", e.getMessage());

        Map<String, Object> response = new HashMap<>();
        response.put(FIELD_STATUS, FIELD_ERROR);
        response.put(FIELD_MESSAGE, e.getMessage());
        log.warn("Возврат ответа 404 Not Found: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException e) {
        log.error("Обработка исключения IllegalArgumentException: {} ", e.getMessage());

        Map<String, Object> response = new HashMap<>();
        response.put(FIELD_STATUS, FIELD_ERROR);
        response.put(FIELD_MESSAGE, e.getMessage());
        log.warn("Возврат ответа 400 Bad Request: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalState(IllegalArgumentException e) {
        log.error("Обработка исключения IllegalStateException: {} ", e.getMessage());

        Map<String, Object> response = new HashMap<>();
        response.put(FIELD_STATUS, FIELD_ERROR);
        response.put(FIELD_MESSAGE, e.getMessage());
        log.warn("Возврат ответа 409 Conflict: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception e) {
        log.error("Обработка общего исключения: {} ", e.getMessage(), e);

        Map<String, Object> response = new HashMap<>();
        response.put(FIELD_STATUS, FIELD_ERROR);
        response.put(FIELD_MESSAGE, "Внутренняя ошибка сервера");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}