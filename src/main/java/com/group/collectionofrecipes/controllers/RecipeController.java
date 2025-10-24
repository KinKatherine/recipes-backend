package com.group.collectionofrecipes.controllers;

import com.group.collectionofrecipes.dto.recipedto.CreateRecipeDTO;
import com.group.collectionofrecipes.dto.recipedto.RecipeDTO;
import com.group.collectionofrecipes.dto.responsedto.ApiResponse;
import com.group.collectionofrecipes.dto.responsedto.PaginationInfo;
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


    //ГЛАВНАЯ СТРАНИЦА
    @GetMapping("/api/v1/recipes/recipe-of-the-day")
    public ApiResponse<RecipeDTO> getRecipeOfTheDay() {
        log.info("GET /api/v1/recipes/recipe-of-the-day");
        RecipeDTO recipeDTO = recipeService.getRecipeOfTheDay();
        log.info("GET /api/v1/recipes/recipe-of-the-day - рецепт дня с id {} успешно получен", recipeDTO.getId());
        return ApiResponse.success(recipeDTO);
    }

    @GetMapping("/api/v1/recipes/recent")
    public ApiResponse<List<RecipeDTO>> getRecentRecipes(Principal principal) {
        log.info("GET /api/v1/recipes/recent");
        List<RecipeDTO> recentRecipes = recipeService.getLast3AddedRecipes(principal);
        log.info("GET /api/v1/recipes/recent - {} последние рецепты успешно получены", recentRecipes.size());
        return ApiResponse.success(recentRecipes);
    }

    //СТРАНИЦА РЕЦЕПТА
    @GetMapping("/api/v1/recipes/{recipeId}")
    public ApiResponse<RecipeDTO> getRecipeById(@PathVariable Long recipeId, Principal principal) {
        log.info("GET /api/v1/recipes/{}", recipeId);
        RecipeDTO recipeDTO = recipeService.getRecipeById(recipeId, principal);
        log.info("GET /api/v1/recipes/{} - вся информация успешно загружена", recipeId);
        return ApiResponse.success(recipeDTO);
    }

    //СТРАНИЦА ПОЛЬЗОВАТЕЛЯ ИЗБРАННОЕ
    @GetMapping("/api/v1/recipes/favourites")
    public ApiResponse<List<RecipeDTO>> getUserFavouriteRecipes(@RequestParam(defaultValue = "0") int page, Principal principal) {
        log.info("GET /api/v1/recipes/favourites");
        Page<RecipeDTO> recipePage = recipeService.getFavouriteUserRecipes(principal, page);
        log.info("GET /api/v1/recipes/favourites - избранные рецепты пользователя {} успешно получены", principal.getName());
        return ApiResponse.success(recipePage.getContent(), getPaginationInfo(recipePage));
    }

    //СТРАНИЦА ПОЛЬЗОВАТЕЛЯ МОИ ПОДТВЕРЖДЕННЫЕ РЕЦЕПТЫ
    @GetMapping("/api/v1/recipes/my-recipes")
    public ApiResponse<List<RecipeDTO>> getUserAddedConfirmedRecipes(@RequestParam(defaultValue = "0") int page, Principal principal) {
        log.info("GET /api/v1/recipes/my-recipes");
        Page<RecipeDTO> recipePage = recipeService.getUserAddedConfirmedRecipes(principal, page);
        log.info("GET /api/v1/recipes/my-recipes - подтвержненные рецепты пользователя {} успешно получены", principal.getName());
        return ApiResponse.success(recipePage.getContent(), getPaginationInfo(recipePage));
    }

    //СТРАНИЦА АДМИНИСТРАТОРА НЕПОДТВЕРЖДЕННЫЕ РЕЦЕПТЫ
    @GetMapping("/api/v1/recipes/unconfirmed")
    public ApiResponse<List<RecipeDTO>> getUnconfirmedRecipes(@RequestParam(defaultValue = "0") int page) {
        log.info("GET /api/v1/recipes/unconfirmed");
        Page<RecipeDTO> recipePage = recipeService.getUnconfirmedRecipes(page);
        log.info("GET /api/v1/recipes/unconfirmed - неподтвержденные рецепты для админа успешно получены");
        return ApiResponse.success(recipePage.getContent(), getPaginationInfo(recipePage));
    }

    //ПОПУЛЯРНЫЕ РЕЦЕПТЫ
    @GetMapping("/api/v1/recipes/popular")
    public ApiResponse<List<RecipeDTO>> getPopularRecipes(Principal principal) {
        log.info("GET /api/v1/recipes/popular");
        List<RecipeDTO> recipeDTOList = recipeService.getPopularRecipes(principal);
        log.info("GET /api/v1/recipes/popular - {}  популярных рецептов успешно получено", recipeDTOList.size());
        return ApiResponse.success(recipeDTOList);
    }


    //ОДОБРЕНИЕ РЕЦЕПТА АДМИНОМ
    // добавить сохранение ингредиента
    @PutMapping("/api/v1/recipes/confirm/{id}")
    public ApiResponse<Object> confirmRecipe(@PathVariable Long id) {
        recipeService.confirmRecipe(id);
        return ApiResponse.success();
    }


    @PostMapping("/api/v1/recipes")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                    schema = @Schema(implementation = CreateRecipeDTO.class))
    )
    public ApiResponse<RecipeDTO> createRecipe(@RequestPart("recipe") @Valid CreateRecipeDTO createRecipeDTO,
                                               @RequestPart("image") MultipartFile image,
                                               Principal principal) {
        RecipeDTO recipeDTO = recipeService.saveRecipe(createRecipeDTO, image, principal);
        return ApiResponse.success(recipeDTO);
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

}
