package com.group.collectionofrecipes.controllers;

import com.group.collectionofrecipes.dto.categorydto.CategoryDTO;
import com.group.collectionofrecipes.dto.categorydto.CreateCategoryDTO;
import com.group.collectionofrecipes.dto.recipedto.RecipeDTO;
import com.group.collectionofrecipes.dto.responsedto.ApiResponse;
import com.group.collectionofrecipes.dto.responsedto.PaginationInfo;
import com.group.collectionofrecipes.services.CategoryService;
import com.group.collectionofrecipes.services.RecipeService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.group.collectionofrecipes.utils.ApiConstants.FIELD_ERROR;
import static com.group.collectionofrecipes.utils.ApiConstants.FIELD_MESSAGE;
import static com.group.collectionofrecipes.utils.ApiConstants.FIELD_STATUS;


@Tag(name = "Categories")
@RestController
@RequiredArgsConstructor
@Slf4j
public class CategoryController {

    private final CategoryService categoryService;
    private final RecipeService recipeService;

    @GetMapping("/api/v1/categories")
    public ApiResponse<List<CategoryDTO>> getAllCategories() {
        log.info("GET /api/v1/categories");
        List<CategoryDTO> categories = categoryService.findAllCategories();
        log.info("GET /api/v1/categories - возвращено {} категорий", categories.size());
        return ApiResponse.success(categories);
    }

    @GetMapping("/api/v1/categories/{categoryId}/recipes")
    public ApiResponse<List<RecipeDTO>> getCategoryRecipes(@PathVariable Long categoryId,
                                                           @RequestParam(defaultValue = "0") int page,
                                                           Principal principal) {
        log.info("GET /api/v1/category/{}/recipes", categoryId);
        Page<RecipeDTO> recipeDTOPage = recipeService.getRecipesByCategoryId(categoryId,principal,page);
        log.info("GET /api/v1/category/{}/recipes - Успешно возвращено {} рецептов", categoryId, recipeDTOPage.getContent().size());
        return ApiResponse.success(recipeDTOPage.getContent(), (PaginationInfo)recipeDTOPage);
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
    public ResponseEntity<Map<String, Object>> handleIllegalState(IllegalStateException e) {
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





   //ПОКА НЕ НАДО
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/api/v1/categories")
    public ApiResponse<CategoryDTO> createCategory(@RequestBody @Valid CreateCategoryDTO createCategoryDTO) {
        log.info("POST /api/v1/categories/admin/create");
        CategoryDTO categoryDTO = categoryService.saveCategory(createCategoryDTO);
        log.info("POST /api/v1/categories/admin/create - Категория успешно создана: ID={}, Name={}",
                categoryDTO.getId(), categoryDTO.getName());
        return ApiResponse.success(categoryDTO);
    }

    //не надо
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/api/v1/categories/{id}")
    public ApiResponse<CategoryDTO> deleteCategory(@PathVariable Long id) {
        log.info("DELETE /api/v1/categories/admin/delete/{}", id);
        CategoryDTO categoryDTO = categoryService.deleteCategory(id);
        log.info("DELETE /api/v1/categories/admin/delete/{} - Категория успешно удалена: {}", id, categoryDTO.getName());
        return ApiResponse.success(categoryDTO);
    }

    //это вообще надо???
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/api/v1/categories/{id}")
    public ApiResponse<CategoryDTO> updateCategory(@PathVariable Long id,
                                                              @RequestBody @Valid CreateCategoryDTO createCategoryDTO) {
        log.info("PUT /api/v1/categories/admin/update/{}", id);
        log.debug("Новые данные: name={}, description={}",
                createCategoryDTO.getName(), createCategoryDTO.getDescription());

        CategoryDTO updatedCategory = categoryService.updateCategory(id, createCategoryDTO);
        log.info("PUT /api/v1/categories/admin/update/{} - Категория успешно обновлена: {}",
                id, updatedCategory.getName());
        return ApiResponse.success(updatedCategory);
    }

    @GetMapping("/api/v1/categories/{id}") //наверное закрытый...или вообще не нужен
    public ApiResponse<CategoryDTO> getCategoryById(@PathVariable Long id) {
        log.info("GET /api/v1/categories/{}", id);
        CategoryDTO categoryDTO = categoryService.findCategoryById(id);
        log.info("GET /api/v1/categories/{} - Успешно возвращена категория: {}", id, categoryDTO.getName());
        return ApiResponse.success(categoryDTO);
    }
}