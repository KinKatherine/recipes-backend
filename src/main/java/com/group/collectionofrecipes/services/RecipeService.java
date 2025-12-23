package com.group.collectionofrecipes.services;

import com.group.collectionofrecipes.dto.commentdto.CommentDTO;
import com.group.collectionofrecipes.dto.ingredientdto.CreateIngredientDTO;
import com.group.collectionofrecipes.dto.ingredientdto.IngredientDTO;
import com.group.collectionofrecipes.dto.ratingdto.RatingStatsProjection;
import com.group.collectionofrecipes.dto.ratingdto.RecipeRatingProjection;
import com.group.collectionofrecipes.dto.recipedto.CreateRecipeDTO;
import com.group.collectionofrecipes.dto.recipedto.RecipeDTO;
import com.group.collectionofrecipes.entities.Category;
import com.group.collectionofrecipes.entities.Favourite;
import com.group.collectionofrecipes.entities.Ingredient;
import com.group.collectionofrecipes.entities.Recipe;
import com.group.collectionofrecipes.entities.RecipeIngredientMapping;
import com.group.collectionofrecipes.entities.User;
import com.group.collectionofrecipes.enums.Unit;
import com.group.collectionofrecipes.exceptions.NoRecipesFoundException;
import com.group.collectionofrecipes.exceptions.SaveFileException;
import com.group.collectionofrecipes.exceptions.SaveRecipeException;
import com.group.collectionofrecipes.exceptions.UnauthorizedUserException;
import com.group.collectionofrecipes.mappers.CommentMapper;
import com.group.collectionofrecipes.mappers.FavouriteMapper;
import com.group.collectionofrecipes.mappers.IngredientMapper;
import com.group.collectionofrecipes.mappers.RecipeMapper;
import com.group.collectionofrecipes.repositories.CategoryRepository;
import com.group.collectionofrecipes.repositories.FavouriteRepository;
import com.group.collectionofrecipes.repositories.IngredientRepository;
import com.group.collectionofrecipes.repositories.RatingRepository;
import com.group.collectionofrecipes.repositories.RecipeIngredientRepository;
import com.group.collectionofrecipes.repositories.RecipeRepository;
import com.group.collectionofrecipes.repositories.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.group.collectionofrecipes.utils.ApiConstants.ERROR_RECIPE_NOT_FOUND;
import static com.group.collectionofrecipes.utils.ApiConstants.FIXED_PAGE_SIZE;
import static com.group.collectionofrecipes.utils.ApiConstants.UNAUTHORIZED_USER;

@Service
@Slf4j
@RequiredArgsConstructor
public class RecipeService {

    private final LocalFileStorageService fileStorageService;
    private final RecipeRepository recipeRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final RecipeMapper recipeMapper;
    private final IngredientMapper ingredientMapper;
    private final CommentMapper commentMapper;
    private final FavouriteRepository favouriteRepository;
    private final RatingRepository ratingRepository;
    private final IngredientRepository ingredientRepository;
    private final RecipeIngredientRepository recipeIngredientRepository;
    private final FavouriteMapper  favouriteMapper;


    @Transactional
    @Cacheable(
            value = "recipeOfTheDay",
            key = "T(java.time.LocalDate).now(T(java.time.ZoneId).of('Europe/Moscow'))"
    )
    public RecipeDTO getRecipeOfTheDay() {
        log.info("Запрос на получение рецепта дня");
        LocalDate today = LocalDate.now(ZoneId.of("Europe/Moscow"));
        String dailySeed = today.toString();

        Recipe newRecipe = recipeRepository.findRandomRecipeWithSeed(dailySeed)
                .orElseThrow(() -> new NoRecipesFoundException("No recipes found"));

        log.info("Найден рецепт дня");
        return recipeMapper.toRecipeDto(newRecipe);
    }


    @Transactional(readOnly = true)
    public List<RecipeDTO> getLast3AddedRecipes(Principal principal) {
        log.info("Запрос на получение 3 последних добавленных рецептов");
        Pageable topThree = PageRequest.of(0, 3);
        final String currentUsername = (principal != null) ? principal.getName() : null;
        List<Recipe> recipeList = recipeRepository.findLatestRecipes(topThree);

        if (recipeList.isEmpty()) {
            throw new NoRecipesFoundException("No recipes found");
        }

        List<Long> ids = recipeList.stream().map(Recipe::getId).toList();
        List<RecipeRatingProjection> ratingList = recipeRepository.findAverageRatingsForRecipes(ids);
        Map<Long, Double> ratingMap = getAverageRatingsMap(ratingList);

        log.info("Найдены 3 последние рецепты");
        return recipeList.stream()
                .map(recipe -> mapRecipeWithRatingAndFavorite(recipe, ratingMap, currentUsername))
                .toList();
    }


    @Transactional(readOnly = true)
    public RecipeDTO getRecipeById(Long recipeId, Principal principal) {
        log.info("Запрос на получение рецепта по ID: {}", recipeId);
        Recipe recipe = recipeRepository.findByIdWithMainDetails(recipeId)
                .orElseThrow(() -> new EntityNotFoundException(ERROR_RECIPE_NOT_FOUND + recipeId));

        log.info("Рецепт с ID {} успешно найден: {}", recipeId, recipe.getTitle());
        log.info("Успешно загружен автор и  категория");

        List<IngredientDTO> ingredientDTOS = recipeRepository.loadIngredientMappersByRecipeId(recipeId).stream()
                .map(i -> {
                    IngredientDTO dto = ingredientMapper.toIngredientDto(i.getIngredient());
                    dto.setAmount(i.getAmount());
                    dto.setStringUnit(i.getUnit().getLabel());
                    return dto;
                })
                .toList();
        log.info("Успешно загружены ингредиенты");

        List<CommentDTO> commentDTOS = recipeRepository.loadCommentsByRecipeId(recipeId).stream()
                .map(commentMapper::toCommentDto)
                .toList();
        log.info("Успешно загружены комментарии");

        RecipeDTO recipeDTO = recipeMapper.toRecipeDto(recipe);
        RatingStatsProjection statsProjection = recipeRepository.calculateRatingStats(recipeId);

        recipeDTO.setAverageRating(statsProjection.getAverageRating());
        recipeDTO.setCountOfRatings(statsProjection.getCount());
        recipeDTO.setIngredientDTOs(ingredientDTOS);
        recipeDTO.setCommentDTOs(commentDTOS);
        recipeDTO.setCommentsCount(commentDTOS.size());

        if (principal != null) {
            String username = principal.getName();
            ratingRepository.findByRecipeIdAndUserUsername(recipeId, username)
                    .ifPresentOrElse(
                            rating -> recipeDTO.setUserRating(rating.getEstimation()),
                            () -> recipeDTO.setUserRating(0)
                    );

            recipeDTO.setIsFavourite(
                    favouriteRepository.existsByRecipeIdAndUserUsername(recipe.getId(), username)
            );
        }
        log.info("Найден рецепт по id {}", recipeId);
        return recipeDTO;
    }


    @Transactional(readOnly = true)
    public Page<RecipeDTO> getFavouriteUserRecipes(Principal principal, int pageNumber) {
        log.info("Запрос на получение избранный рецептов пользователя");
        if (principal == null) {
            throw new UnauthorizedUserException(UNAUTHORIZED_USER);
        }
        String username = principal.getName();
        Pageable pageRequest = PageRequest.of(
                pageNumber,
                FIXED_PAGE_SIZE,
                Sort.by("fb.addedAt").descending()
        );

        Page<Recipe> favoriteRecipesPage =
                recipeRepository.findConfirmedFavoriteRecipesByUsername(username, pageRequest);

        List<Long> recipeIds = favoriteRecipesPage.getContent().stream().map(Recipe::getId).toList();
        Map<Long, Double> ratingMap = getAverageRatingsMap(recipeRepository.findAverageRatingsForRecipes(recipeIds));

        log.info("Найдено {} избранных  рецептов пользователя", favoriteRecipesPage.getTotalElements());
        return favoriteRecipesPage.map(recipe -> mapRecipeWithRatingAndFavorite(recipe, ratingMap, username));
    }

    @Transactional(readOnly = true)
    public List<RecipeDTO> sortRecipesByCookingTime(Principal principal, int pageNumber) {
        log.info("Запрос на сортировку репептов по времени готовки");

        String username = (principal != null) ? principal.getName() : null;
        Pageable pageRequest = PageRequest.of(
                pageNumber,
                FIXED_PAGE_SIZE,
                Sort.by("cookingTime").ascending()
        );
        Page<Recipe> sortedRecipesPage =
                recipeRepository.sortRecipesByCookingTime(pageRequest);

        List<Long> recipeIds = sortedRecipesPage.getContent().stream().map(Recipe::getId).toList();
        Map<Long, Double> ratingMap = getAverageRatingsMap(recipeRepository.findAverageRatingsForRecipes(recipeIds));


        log.info("{} рецептов отсортировано по времени приготовления", sortedRecipesPage.getTotalElements());
        return sortedRecipesPage.stream()
                        .map(recipe -> mapRecipeWithRatingAndFavorite(recipe, ratingMap, username))
                        .toList();
    }


    @Transactional(readOnly = true)
    public Page<RecipeDTO> getUnconfirmedRecipes(int pageNumber) {
        log.info("Запрос на получение неподтвержденныз рецептов");
        Pageable pageRequest = PageRequest.of(
                pageNumber,
                FIXED_PAGE_SIZE,
                Sort.by("createdAt").descending()
        );

        Page<Recipe> unconfirmedRecipesPage = recipeRepository.findUnconfirmedRecipes(pageRequest);

        log.info("Найдено {} неподтвержденных рецептов", unconfirmedRecipesPage.getTotalElements());
        return unconfirmedRecipesPage.map(recipe -> {
            RecipeDTO recipeDTO = recipeMapper.toRecipeDto(recipe);
            recipeDTO.setIsFavourite(false);
            recipeDTO.setAverageRating(0);
            return recipeDTO;
        });
    }


    @Transactional
    public void confirmRecipe(Long id) {
        log.info("Запрос подтверждения рецепта с id: {}", id);

        Recipe recipe = recipeRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Рецепт с id {} не найден", id);
                    return new EntityNotFoundException("Неверный id рецепта");
                });

        recipe.setIsConfirmed(true);

        List<Ingredient> ingredients = recipe.getIngredientMappings().stream()
                .map(RecipeIngredientMapping::getIngredient)
                .filter(ing -> !Boolean.TRUE.equals(ing.getIsConfirmed()))
                .toList();

        for (Ingredient ing : ingredients) {
            ing.setIsConfirmed(true);
            log.info("Ингредиент '{}' автоматически подтвержден вместе с рецептом", ing.getName());
        }

        recipeRepository.save(recipe);

        log.info("Рецепт с id {} и его ингредиенты успешно подтверждены", id);
    }


    @Transactional(readOnly = true)
    public Page<RecipeDTO> getUserAddedConfirmedRecipes(Principal principal, int pageNumber) {
        log.info("Запрос на получение подтвержденных рецептов пользователя");
        if (principal == null) {
            throw new UnauthorizedUserException(UNAUTHORIZED_USER);
        }
        String username = principal.getName();
        Pageable pageRequest = PageRequest.of(
                pageNumber,
                FIXED_PAGE_SIZE,
                Sort.by("createdAt").descending()
        );

        Page<Recipe> userConfirmedRecipesPage = recipeRepository.findConfirmedRecipesByAuthorUsername(username, pageRequest);

        List<Long> recipeIds = userConfirmedRecipesPage.getContent().stream().map(Recipe::getId).toList();
        Map<Long, Double> ratingMap = getAverageRatingsMap(recipeRepository.findAverageRatingsForRecipes(recipeIds));
        log.info("Найдено {} подтвержденных рецептов пользователя", userConfirmedRecipesPage.getTotalElements());

        return userConfirmedRecipesPage.map(recipe -> mapRecipeWithRatingAndFavorite(recipe, ratingMap, username));

    }


    @Transactional(readOnly = true)
    public Page<RecipeDTO> getRecipesByCategoryId(Long categoryId, Principal principal, int pageNumber) {
        log.info("Запрос на получение рецептов для категории с ID: {}", categoryId);
        Pageable pageRequest = PageRequest.of(
                pageNumber,
                FIXED_PAGE_SIZE
        );

        Page<Recipe> recipesCategoryPage = recipeRepository.findRecipesByCategoryId(categoryId, pageRequest);
        List<Long> recipeIds = recipesCategoryPage.getContent().stream().map(Recipe::getId).toList();
        Map<Long, Double> ratingMap = getAverageRatingsMap(recipeRepository.findAverageRatingsForRecipes(recipeIds));

        final String currentUsername = (principal != null) ? principal.getName() : null;
        log.info("Найдено {} рецептов категории с id {}", recipesCategoryPage.getTotalElements(), categoryId);
        return recipesCategoryPage.map(recipe -> mapRecipeWithRatingAndFavorite(recipe, ratingMap, currentUsername));
    }


    @Transactional(readOnly = true)
    public List<RecipeDTO> getPopularRecipes(Principal principal) {

        log.info("Зпрос на получение списка популярных рецептов");
        List<Recipe> recipes = recipeRepository.findTop10ByRatingAndVotesCount();
        List<Long> recipeIds = recipes.stream().map(Recipe::getId).toList();
        Map<Long, Double> ratingMap = getAverageRatingsMap(recipeRepository.findAverageRatingsForRecipes(recipeIds));

        final String currentUsername = (principal != null) ? principal.getName() : null;

        log.info("Популярные рецепты найдены");
        return recipes.stream()
                .map(recipe -> mapRecipeWithRatingAndFavorite(recipe, ratingMap, currentUsername))
                .toList();
    }



    private Map<Long, Double> getAverageRatingsMap(List<RecipeRatingProjection> ratingList) {
        return ratingList.stream()
                .collect(Collectors.toMap(RecipeRatingProjection::getId, RecipeRatingProjection::getAverageRating));
    }

    private RecipeDTO mapRecipeWithRatingAndFavorite(Recipe recipe, Map<Long, Double> ratingMap, String currentUsername) {
        RecipeDTO dto = recipeMapper.toRecipeDto(recipe);
        Double rawRating = ratingMap.getOrDefault(recipe.getId(), 0.0);
        dto.setAverageRating((int) Math.round(rawRating));
        boolean isFavourite = currentUsername != null &&
                favouriteRepository.existsByRecipeIdAndUserUsername(recipe.getId(), currentUsername);
        dto.setIsFavourite(isFavourite);

        return dto;
    }


    @Transactional
    public RecipeDTO saveRecipe(CreateRecipeDTO createRecipeDTO, MultipartFile image, List<CreateIngredientDTO> ingredientDTOS, Principal principal) {

        log.info("Запрос на создание нового рецепта: {}", createRecipeDTO.getTitle());

        if (principal == null) {
            log.error(UNAUTHORIZED_USER);
            throw  new UnauthorizedUserException(UNAUTHORIZED_USER);
        }

        String imageName = null;
        Recipe savedRecipe;

        try {
            imageName = fileStorageService.storeImageFile(image);
            log.info("Изображение для рецепта {} успешно сохранено: {}", createRecipeDTO.getTitle(), imageName);

            Recipe recipe = collectRecipe(createRecipeDTO, principal, imageName);
            savedRecipe = recipeRepository.save(recipe);
            log.info("Рецепт успешно создан: ID={}, Name={}", savedRecipe.getId(), savedRecipe.getTitle());

            saveIngredients(ingredientDTOS, savedRecipe);

        } catch (SaveFileException e) {
            log.error("Не удалось сохранить картинку для рецепта: {}. Ошибка: {}", createRecipeDTO.getTitle(), e.getMessage());
            throw new SaveRecipeException("Failed to save recipe image: " + e.getMessage());
        } catch (Exception dbException) {
            if (imageName != null) {
                fileStorageService.deleteImageFile(imageName);
                log.warn("Откат: Файл {} удалён из-за ошибки транзакции БД.", imageName);
            }
            throw dbException;
        }
        return recipeMapper.toRecipeDto(savedRecipe);
    }


    private Recipe collectRecipe(CreateRecipeDTO createRecipeDTO, Principal principal, String imageName) {
        Category category = categoryRepository.findById(createRecipeDTO.getCategoryId()).orElseThrow(()
                -> new EntityNotFoundException("Категория не найдена по id"));
        User user = userRepository.findByUsername(principal.getName()).orElseThrow(()
                -> new EntityNotFoundException("Пользователь не найден по ID"));

        return recipeMapper.toRecipeEntity(createRecipeDTO, user, category, imageName);
    }

    private void saveIngredients(List<CreateIngredientDTO> ingredientDTOS, Recipe savedRecipe) {

        Set<String> formattedNames = ingredientDTOS.stream()
                .map(dto -> formatIngredientName(dto.getName()))
                .collect(Collectors.toSet());

        List<Ingredient> existingIngredients = ingredientRepository.findAllByNameIn(formattedNames);
        Map<String, Ingredient> existingMap = new HashMap<>();

        for (Ingredient ing : existingIngredients) {
            String key = ing.getName();
            if (!existingMap.containsKey(key)) {
                existingMap.put(key, ing);
            } else {
                Ingredient currentInMap = existingMap.get(key);
                if (Boolean.TRUE.equals(ing.getIsConfirmed())
                        && !Boolean.TRUE.equals(currentInMap.getIsConfirmed())) {
                    existingMap.put(key, ing);
                }
            }
        }

        List<RecipeIngredientMapping> mappingsToSave = new ArrayList<>();

        for (CreateIngredientDTO dto : ingredientDTOS) {
            String formattedName = formatIngredientName(dto.getName());
            Ingredient ingredient;

            if (existingMap.containsKey(formattedName)) {
                ingredient = existingMap.get(formattedName);
            } else {
                ingredient = ingredientMapper.toIngredientEntity(dto);

                ingredient.setName(formattedName);
                ingredient = ingredientRepository.save(ingredient);
                existingMap.put(formattedName, ingredient);
            }

            mappingsToSave.add(RecipeIngredientMapping.builder()
                    .unit(Unit.findByLabel(dto.getUnit()))
                    .amount(dto.getAmount())
                    .recipe(savedRecipe)
                    .ingredient(ingredient)
                    .build());
        }

        recipeIngredientRepository.saveAll(mappingsToSave);
    }

    private String formatIngredientName(String rawName) {
        String trimmed = rawName.trim();
        return trimmed.substring(0, 1).toUpperCase() + trimmed.substring(1).toLowerCase();
    }

    @Transactional
    public void deleteRecipe(Long id) {
        log.info("Запрос на удаление рецепта с ID: {}", id);

        Recipe recipe = recipeRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Рецепт с ID {} не найден для удаления", id);
                    return new EntityNotFoundException(ERROR_RECIPE_NOT_FOUND + id);
                });

        String imageName = recipe.getImage();

        List<Ingredient> candidatesForDeletion = recipe.getIngredientMappings().stream()
                .map(RecipeIngredientMapping::getIngredient)
                .filter(ing -> !Boolean.TRUE.equals(ing.getIsConfirmed()))
                .toList();

        recipeRepository.delete(recipe);
        recipeRepository.flush();

        for (Ingredient ingredient : candidatesForDeletion) {
            long usageCount = recipeIngredientRepository.countByIngredientId(ingredient.getId());

            if (usageCount == 0) {
                ingredientRepository.delete(ingredient);
                log.info("Неиспользуемый неподтвержденный ингредиент '{}' удален", ingredient.getName());
            }
        }
        if (imageName != null && !imageName.isBlank()) {
            try {
                fileStorageService.deleteImageFile(imageName);
                log.info("Файл изображения '{}' удален", imageName);
            } catch (Exception e) {
                log.warn("Не удалось удалить файл изображения '{}': {}", imageName, e.getMessage());
            }
        }

        log.info("Рецепт с ID {} успешно удален", id);
    }

    public RecipeDTO updateRecipe(Long id, CreateRecipeDTO createRecipeDTO) {

        log.info("Запрос на обновление рецепта с ID: {}, новые данные: {}",
                id, createRecipeDTO.getTitle());
        try {
            Recipe recipe = recipeRepository.findById(id)
                    .orElseThrow(() -> {
                        log.error("Рецепт с ID {} не найден для обновления", id);
                        return new EntityNotFoundException(ERROR_RECIPE_NOT_FOUND + id);
                    });

            recipe.setTitle(createRecipeDTO.getTitle());
            recipe.setDescription(createRecipeDTO.getDescription());
            recipe.setInstruction(createRecipeDTO.getInstruction());
            recipe.setCookingTime(createRecipeDTO.getCookingTime());
            recipe.setCountOfServings(createRecipeDTO.getCountOfServings());

            Recipe updatedRecipe = recipeRepository.save(recipe);
            log.info("Рецепт с ID {} успешно обновлен: {}", id, updatedRecipe.getTitle());
            return recipeMapper.toRecipeDto(updatedRecipe);
        } catch (DataIntegrityViolationException e) {
            log.error("Ошибка при обновлении рецепта: рецепт с названием '{}' уже существует",
                    createRecipeDTO.getTitle());
            throw new IllegalArgumentException("Рецепт с таким названием уже существует");
        }
    }

    @Transactional(readOnly = true)
    public List<RecipeDTO> findRecipeByTitle(String title, Principal principal) {

        List<Recipe> recipes = recipeRepository.findByTitleContainingIgnoreCase(title);
        final String currentUsername = (principal != null) ? principal.getName() : null;
        List<Long> ids = recipes.stream().map(Recipe::getId).toList();
        List<RecipeRatingProjection> ratingList = recipeRepository.findAverageRatingsForRecipes(ids);
        Map<Long, Double> ratingMap = getAverageRatingsMap(ratingList);

        log.info("Найдено {} рецептов  по названию {}", recipes.size(), title);
        return recipes.stream()
                .map(recipe -> mapRecipeWithRatingAndFavorite(recipe, ratingMap, currentUsername))
                .toList();
    }


    @Transactional
    public void addToFavourites(Long recipeId, Principal principal) {
        if (principal == null) {
            log.error(UNAUTHORIZED_USER);
            throw new UnauthorizedUserException(UNAUTHORIZED_USER);
        }

        String username = principal.getName();
        if (favouriteRepository.existsByRecipeIdAndUserUsername(recipeId, username)) {
            log.warn("Пользователь {} уже добавил рецепт ID {}.", username, recipeId);
            throw new IllegalStateException("Рецепт уже в избранном.");
        }

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("Пользователь не найден"));

        Recipe recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new EntityNotFoundException("Рецепт не найден"));

        Favourite favourite = favouriteMapper.toFavouriteEntity(recipe, user);
        favouriteRepository.save(favourite);

        log.info("Рецепт ID {} добавлен в избранное пользователем {}", recipeId, username);
    }

    @Transactional
    public void removeFromFavourites(Long recipeId, Principal principal) {
        if (principal == null) {
            throw new UnauthorizedUserException(UNAUTHORIZED_USER);
        }

        String username = principal.getName();
        Favourite favourite = favouriteRepository.findByRecipeIdAndUserUsername(recipeId, username)
                .orElseThrow(() -> {
                    log.warn("Попытка удаления несуществующего избранного: user {}, recipe {}", username, recipeId);
                    return new EntityNotFoundException("Рецепт не найден в вашем списке избранного.");
                });

        favouriteRepository.delete(favourite);
        log.info("Рецепт ID {} удален из избранного пользователем {}", recipeId, username);
    }
    @Transactional(readOnly = true)
    public Page<RecipeDTO> searchRecipes(String query, Long categoryId, Long authorId,
                                         int page, String sortBy, String sortDirection,
                                         Principal principal) {
        log.info("Запрос на расширенный поиск рецептов");

        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        Pageable pageable = PageRequest.of(page, FIXED_PAGE_SIZE, sort);

        Page<Recipe> recipesPage = recipeRepository.searchRecipes(query, categoryId, authorId, pageable);

        List<Long> recipeIds = recipesPage.getContent().stream().map(Recipe::getId).toList();
        Map<Long, Double> ratingMap = getAverageRatingsMap(recipeRepository.findAverageRatingsForRecipes(recipeIds));
        final String currentUsername = (principal != null) ? principal.getName() : null;

        log.info("Найдено {} рецептов по запросу", recipesPage.getTotalElements());
        return recipesPage.map(recipe -> mapRecipeWithRatingAndFavorite(recipe, ratingMap, currentUsername));
    }

}
