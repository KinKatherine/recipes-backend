package com.group.collectionofrecipes.controllers;

import com.group.collectionofrecipes.dto.ingredientdto.CreateIngredientDTO;
import com.group.collectionofrecipes.dto.recipedto.CreateRecipeDTO;
import com.group.collectionofrecipes.dto.recipedto.RecipeDTO;
import com.group.collectionofrecipes.dto.responsedto.ApiResponse;
import com.group.collectionofrecipes.dto.responsedto.PaginationInfo;
import com.group.collectionofrecipes.exceptions.DeleteFileException;
import com.group.collectionofrecipes.exceptions.NoRecipesFoundException;
import com.group.collectionofrecipes.exceptions.UnauthorizedUserException;
import com.group.collectionofrecipes.services.RecipeService;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
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
import static com.group.collectionofrecipes.utils.ApiConstants.FIELD_STATUS;


@Tag(name = "Recipes")
@Slf4j
@RestController
@RequiredArgsConstructor
public class RecipeController {

    private final RecipeService recipeService;


    @GetMapping("/api/v1/recipes/recipe-of-the-day")
    public ApiResponse<RecipeDTO> getRecipeOfTheDay(HttpServletRequest request) {
        String clientIp = getClientIpAddress(request);
        log.info("GET /api/v1/recipes/recipe-of-the-day from IP: {}", clientIp);
        RecipeDTO recipeDTO = recipeService.getRecipeOfTheDay();
        log.info("GET /api/v1/recipes/recipe-of-the-day - рецепт дня с id {} успешно получен для IP: {}",
                recipeDTO.getId(), clientIp);
        return ApiResponse.success(recipeDTO);
    }


    @GetMapping("/api/v1/recipes/recent")
    public ApiResponse<List<RecipeDTO>> getRecentRecipes(Principal principal) {
        log.info("GET /api/v1/recipes/recent");
        List<RecipeDTO> recentRecipes = recipeService.getLast3AddedRecipes(principal);
        log.info("GET /api/v1/recipes/recent - {} последние рецепты успешно получены", recentRecipes.size());
        return ApiResponse.success(recentRecipes);
    }


    @GetMapping("/api/v1/recipes/{recipeId}")
    public ApiResponse<RecipeDTO> getRecipeById(@PathVariable Long recipeId, Principal principal) {
        log.info("GET /api/v1/recipes/{}", recipeId);
        RecipeDTO recipeDTO = recipeService.getRecipeById(recipeId, principal);
        log.info("GET /api/v1/recipes/{} - вся информация успешно загружена", recipeId);
        return ApiResponse.success(recipeDTO);
    }


    @GetMapping("/api/v1/recipes/favourites")
    public ApiResponse<List<RecipeDTO>> getUserFavouriteRecipes(@RequestParam(defaultValue = "0") int page, Principal principal) {
        log.info("GET /api/v1/recipes/favourites");
        Page<RecipeDTO> recipePage = recipeService.getFavouriteUserRecipes(principal, page);
        log.info("GET /api/v1/recipes/favourites - избранные рецепты пользователя {} успешно получены", principal.getName());
        return ApiResponse.success(recipePage.getContent(), getPaginationInfo(recipePage));
    }


    @GetMapping("/api/v1/recipes/my-recipes")
    public ApiResponse<List<RecipeDTO>> getUserAddedConfirmedRecipes(@RequestParam(defaultValue = "0") int page, Principal principal) {
        log.info("GET /api/v1/recipes/my-recipes");
        Page<RecipeDTO> recipePage = recipeService.getUserAddedConfirmedRecipes(principal, page);
        log.info("GET /api/v1/recipes/my-recipes - подтвержненные рецепты пользователя {} успешно получены", principal.getName());
        return ApiResponse.success(recipePage.getContent(), getPaginationInfo(recipePage));
    }


    @GetMapping("/api/v1/recipes/unconfirmed")
    public ApiResponse<List<RecipeDTO>> getUnconfirmedRecipes(@RequestParam(defaultValue = "0") int page) {
        log.info("GET /api/v1/recipes/unconfirmed");
        Page<RecipeDTO> recipePage = recipeService.getUnconfirmedRecipes(page);
        log.info("GET /api/v1/recipes/unconfirmed - неподтвержденные рецепты для админа успешно получены");
        return ApiResponse.success(recipePage.getContent(), getPaginationInfo(recipePage));
    }


    @GetMapping("/api/v1/recipes/popular")
    public ApiResponse<List<RecipeDTO>> getPopularRecipes(Principal principal) {
        log.info("GET /api/v1/recipes/popular");
        List<RecipeDTO> recipeDTOList = recipeService.getPopularRecipes(principal);
        log.info("GET /api/v1/recipes/popular - {}  популярных рецептов успешно получено", recipeDTOList.size());
        return ApiResponse.success(recipeDTOList);
    }


    @PutMapping("/api/v1/recipes/unconfirmed/confirm/{id}")
    public ApiResponse<Object> confirmUnconfirmedRecipe(@PathVariable Long id) {
        log.info("PUT  /api/v1/recipes/unconfirmed/confirm/{id}");
        recipeService.confirmRecipe(id);
        log.info("PUT  /api/v1/recipes/unconfirmed/confirm/{id} - рецепт с id {} подтвержден админом", id);
        return ApiResponse.success();
    }

    @DeleteMapping("/api/v1/recipes/unconfirmed/delete/{id}")
    public ApiResponse<Object> deleteUnconfirmedRecipe(@PathVariable Long id) {
        log.info("DELETE  /api/v1/recipes/unconfirmed/delete/{id}");
        recipeService.deleteRecipe(id);
        log.info("DELETE  /api/v1/recipes/unconfirmed/delete/{id} - рецепт с id {} удален админом", id);
        return ApiResponse.success();
    }


    @PostMapping("/api/v1/recipes")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                    schema = @Schema(implementation = CreateRecipeDTO.class))
    )
    public ApiResponse<RecipeDTO> createRecipe(@RequestPart("recipe") @Valid CreateRecipeDTO createRecipeDTO,
                                               @RequestPart("image") MultipartFile image,
                                               @RequestPart("ingredients") @Valid List<CreateIngredientDTO> ingredientDTOS,
                                               Principal principal) {
        log.info("GET /api/v1/recipes");
        RecipeDTO recipeDTO =  recipeService.saveRecipe(createRecipeDTO, image, ingredientDTOS, principal);
        log.info("GET /api/v1/recipes -создан рецепт с id {}", recipeDTO.getId());
        return ApiResponse.success();
    }


    @GetMapping("/api/v1/recipes/title")
    public ApiResponse<List<RecipeDTO>> findRecipeByTitle(@RequestParam(name = "title") String title,
                                                          Principal principal) {
        log.info("GET /api/v1/recipes/title {}", title);
        List<RecipeDTO> recipeDTOList = recipeService.findRecipeByTitle(title, principal);
        log.info("GET /api/v1/recipes/title {} - найдено {} рецептов", title, recipeDTOList.size());
        return ApiResponse.success(recipeDTOList);
    }


    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleEntityNotFound(EntityNotFoundException e) {
        log.warn("Обработка исключения EntityNotFoundException: {} ", e.getMessage());

        Map<String, Object> response = new HashMap<>();
        response.put(FIELD_STATUS, FIELD_ERROR);
        response.put(FIELD_MESSAGE, e.getMessage());
        log.warn("Возврат ответа 404 Not Found: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(DeleteFileException.class)
    public ResponseEntity<Map<String, Object>> handleDeleteFile(DeleteFileException e) {
        log.warn("Обработка исключения DeleteFileException: {} ", e.getMessage());

        Map<String, Object> response = new HashMap<>();
        response.put(FIELD_STATUS, FIELD_ERROR);
        response.put(FIELD_MESSAGE, e.getMessage());
        log.warn("Возврат ответа 400 Not Found: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(NoRecipesFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNoRecipesFound(NoRecipesFoundException e) {
        log.warn("Обработка исключения NoRecipesFoundException: {} ", e.getMessage());

        Map<String, Object> response = new HashMap<>();
        response.put(FIELD_STATUS, FIELD_ERROR);
        response.put(FIELD_MESSAGE, e.getMessage());
        log.warn("Возврат ответа 404 Not Found: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException e) {
        log.warn("Обработка исключения IllegalArgumentException: {} ", e.getMessage());

        Map<String, Object> response = new HashMap<>();
        response.put(FIELD_STATUS, FIELD_ERROR);
        response.put(FIELD_MESSAGE, e.getMessage());
        log.warn("Возврат ответа 400 BAD_REQUEST: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(UnauthorizedUserException.class)
    public ResponseEntity<Map<String, Object>> handleUnauthorizedUser(UnauthorizedUserException e) {
        log.warn("Обработка исключения UnauthorizedUserException: {} ", e.getMessage());

        Map<String, Object> response = new HashMap<>();
        response.put(FIELD_STATUS, FIELD_ERROR);
        response.put(FIELD_MESSAGE, e.getMessage());
        log.warn("Возврат ответа 401 UNAUTHORIZED: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }


    //УТИЛЬНЫЙ МЕТОД
    private PaginationInfo getPaginationInfo(Page<RecipeDTO> dataPage) {
        return PaginationInfo.builder()
                .currentPage(dataPage.getNumber())
                .totalPages(dataPage.getTotalPages())
                .totalItems(dataPage.getTotalElements())
                .pageSize(dataPage.getSize())
                .hasNext(dataPage.hasNext())
                .hasPrevious(dataPage.hasPrevious())
                .build();
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty() && !"unknown".equalsIgnoreCase(xForwardedFor)) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty() && !"unknown".equalsIgnoreCase(xRealIp)) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }

}
