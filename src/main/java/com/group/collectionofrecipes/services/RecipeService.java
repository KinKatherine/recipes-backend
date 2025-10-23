package com.group.collectionofrecipes.services;

import com.group.collectionofrecipes.dto.commentdto.CommentDTO;
import com.group.collectionofrecipes.dto.ingredientdto.IngredientDTO;
import com.group.collectionofrecipes.dto.ratingdto.RatingStatsProjection;
import com.group.collectionofrecipes.dto.ratingdto.RecipeRatingProjection;
import com.group.collectionofrecipes.dto.recipedto.CreateRecipeDTO;
import com.group.collectionofrecipes.dto.recipedto.RecipeDTO;
import com.group.collectionofrecipes.entities.Category;
import com.group.collectionofrecipes.entities.Rating;
import com.group.collectionofrecipes.entities.Recipe;
import com.group.collectionofrecipes.entities.User;
import com.group.collectionofrecipes.exceptions.SaveFileException;
import com.group.collectionofrecipes.exceptions.SaveRecipeException;
import com.group.collectionofrecipes.mappers.CommentMapper;
import com.group.collectionofrecipes.mappers.IngredientMapper;
import com.group.collectionofrecipes.mappers.RecipeMapper;
import com.group.collectionofrecipes.repositories.CategoryRepository;
import com.group.collectionofrecipes.repositories.FavouriteRepository;
import com.group.collectionofrecipes.repositories.RatingRepository;
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
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.group.collectionofrecipes.utils.ApiConstants.ERROR_RECIPE_NOT_FOUND;
import static com.group.collectionofrecipes.utils.ApiConstants.FIXED_PAGE_SIZE;

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


    //ГЛАВНАЯ СТРАНИЦА
    @Transactional
    @Cacheable(
            value = "recipeOfTheDay",
            key = "T(java.time.LocalDate).now(T(java.time.ZoneId).of('Europe/Moscow'))"
    )
    //DONE рецепт дня
    public RecipeDTO getRecipeOfTheDay() {
        LocalDate today = LocalDate.now(ZoneId.of("Europe/Moscow"));
        String dailySeed = today.toString();

        Recipe newRecipe = recipeRepository.findRandomRecipeWithSeed(dailySeed)
                .orElseThrow(() -> new RuntimeException("No recipes found"));

        return recipeMapper.toRecipeDto(newRecipe);
    }

    //DONE 3 последние добавленные
    @Transactional(readOnly = true)
    public List<RecipeDTO> getLast3AddedRecipes(Principal principal) {
        Pageable topThree = PageRequest.of(0, 3);
        final String currentUsername = (principal != null) ? principal.getName() : null;

        List<Recipe> recipeList = recipeRepository.findLatestRecipes(topThree);
        List<Long> ids = recipeList.stream().map(Recipe::getId).toList();
        List<RecipeRatingProjection> ratingList = recipeRepository.findAverageRatingsForRecipes(ids);
        Map<Long, Double> ratingMap = getAverageRatingsMap(ratingList);

        return recipeList.stream()
                .map(recipe -> mapRecipeWithRatingAndFavorite(recipe, ratingMap, currentUsername))
                .toList();
    }


    //СТРАНИЦА РЕЦЕПТА
    //DONE рецепт по id
    @Transactional(readOnly = true)
    public RecipeDTO getRecipeById(Long id, Principal principal) {
        log.info("Запрос на получение рецепта по ID: {}", id);

        Recipe recipe = recipeRepository.findByIdWithMainDetails(id)
                .orElseThrow(() -> new EntityNotFoundException(ERROR_RECIPE_NOT_FOUND + id));

        log.info("Рецепт с ID {} успешно найден: {}", id, recipe.getTitle());
        log.info("Успешно загружен автор, категория и лист избранных" );

        List<IngredientDTO> ingredientDTOS = recipeRepository.loadIngredientMappersByRecipeId(id).stream()
                .map(i -> {
                    IngredientDTO dto = ingredientMapper.toIngredientDto(i.getIngredient());
                    dto.setAmount(i.getAmount());
                    dto.setStringUnit(i.getUnit().getLabel());
                    return dto;
                })
                .toList();
        log.info("Успешно загружены ингредиенты");

        List<CommentDTO> commentDTOS = recipeRepository.loadCommentsByRecipeId(id).stream()
                .map(commentMapper::toCommentDto)
                .toList();
        log.info("Успешно загружены комментарии" );

        RecipeDTO recipeDTO = recipeMapper.toRecipeDto(recipe);
        RatingStatsProjection statsProjection = recipeRepository.calculateRatingStats(id);

        recipeDTO.setAverageRating(statsProjection.getAverageRating());
        recipeDTO.setCountOfRatings(statsProjection.getCount());
        recipeDTO.setIngredientDTOs(ingredientDTOS);
        recipeDTO.setCommentDTOs(commentDTOS);
        recipeDTO.setCommentsCount(commentDTOS.size());

        if(principal!=null){
            String username = principal.getName();
            ratingRepository.findByRecipeIdAndUserUsername(id, username)
                    .ifPresentOrElse(
                            rating -> recipeDTO.setUserRating(rating.getEstimation()),
                            () -> recipeDTO.setUserRating(0)
                    );

            recipeDTO.setIsFavourite(
                    favouriteRepository.existsByRecipeIdAndUserUsername(recipe.getId(), username)
            );
        }
        return recipeDTO;
    }


    //СТРАНИЦА ПОЛЬЗОВАТЕЛЯ ИЗБРАННОЕ
    //DONE
    @Transactional(readOnly = true)
    public Page<RecipeDTO> getFavouriteUserRecipes(Principal principal, int pageNumber) {
        if (principal == null) {
            return Page.empty();
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

        return favoriteRecipesPage.map(recipe -> mapRecipeWithRatingAndFavorite(recipe, ratingMap, username));
    }


    //СТРАНИЦА АДМИНА НЕПОДТВЕРЖДЕННЫЕ РЕЦЕПТЫ
    //DONE
    @Transactional(readOnly = true)
    public Page<RecipeDTO> getUnconfirmedRecipes(int pageNumber){
        Pageable pageRequest = PageRequest.of(
                pageNumber,
                FIXED_PAGE_SIZE,
                Sort.by("createdAt").descending()
        );

        Page<Recipe> unconfirmedRecipesPage = recipeRepository.findUnconfirmedRecipes(pageRequest);

        return unconfirmedRecipesPage.map(recipe -> {
            RecipeDTO recipeDTO = recipeMapper.toRecipeDto(recipe);
            recipeDTO.setIsFavourite(false);
            recipeDTO.setAverageRating(0);
            return recipeDTO;
        });
    }


    //ОДОБРЕНИЕ РЕЦЕПТА АДМИНОМ
    public void confirmRecipe(Long id) {
        log.info("Запрос подтверждения рецепта  с id: {}", id);
        Recipe recipe = recipeRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Рецепт с id {} не найден",id);
                    return new EntityNotFoundException("Неверный id рецепта");
                });

        recipe.setIsConfirmed(true);
        recipeRepository.save(recipe);
        log.info("Рецепт с id {} успешно подтвержден", id);
    }


    //СТРАНИЦА ПОЛЬЗОВАТЕЛЯ МОИ ПОДТВЕРЖДЕННЫЕ РЕЦЕПТЫ
    //DONE
    @Transactional(readOnly = true)
    public Page<RecipeDTO> getUserAddedConfirmedRecipes(Principal principal, int pageNumber){
        if (principal == null) {
            return Page.empty();
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

        return userConfirmedRecipesPage.map(recipe -> mapRecipeWithRatingAndFavorite(recipe, ratingMap, username));

    }


    //ПОЛУЧЕНИЕ РЕЦЕПТО ОПРЕДЕЛЁННОЙ КАТЕГОРИИ
    @Transactional(readOnly = true)
    public Page<RecipeDTO> getRecipesByCategoryId(Long categoryId,Principal principal, int pageNumber) {
        log.info("Запрос на получение рецептов для категории с ID: {}", categoryId);

        Pageable pageRequest = PageRequest.of(
                pageNumber,
                FIXED_PAGE_SIZE
        );

        Page<Recipe> recipesCategoryPage =  recipeRepository.findRecipesByCategoryId(categoryId,pageRequest);
        List<Long> recipeIds = recipesCategoryPage.getContent().stream().map(Recipe::getId).toList();
        Map<Long, Double> ratingMap = getAverageRatingsMap(recipeRepository.findAverageRatingsForRecipes(recipeIds));

        final String currentUsername = (principal != null) ? principal.getName() : null;

        return recipesCategoryPage.map(recipe -> mapRecipeWithRatingAndFavorite(recipe, ratingMap, currentUsername));
    }

    @Transactional(readOnly = true)
    public List<RecipeDTO> getPopularRecipes(Principal principal) {

        List<Recipe> recipes = recipeRepository.findTop10ByRatingAndVotesCount();
        List<Long> recipeIds = recipes.stream().map(Recipe::getId).toList();
        Map<Long, Double> ratingMap = getAverageRatingsMap(recipeRepository.findAverageRatingsForRecipes(recipeIds));

        final String currentUsername = (principal != null) ? principal.getName() : null;

        return recipes.stream()
                .map(recipe -> mapRecipeWithRatingAndFavorite(recipe, ratingMap, currentUsername))
                .toList();
    }


    //УТИЛЬНЫЙ МЕТОД
    private Map<Long, Double> getAverageRatingsMap( List<RecipeRatingProjection> ratingList) {
        return ratingList.stream()
                .collect(Collectors.toMap(RecipeRatingProjection::getId, RecipeRatingProjection::getAverageRating));
    }


    //УТИЛЬНЫЙ МЕТОД
    private RecipeDTO mapRecipeWithRatingAndFavorite(Recipe recipe, Map<Long, Double> ratingMap, String currentUsername) {
        RecipeDTO dto = recipeMapper.toRecipeDto(recipe);
        Double rawRating = ratingMap.getOrDefault(recipe.getId(), 0.0);
        dto.setAverageRating((int) Math.round(rawRating));
        boolean isFavourite = currentUsername != null &&
                favouriteRepository.existsByRecipeIdAndUserUsername(recipe.getId(), currentUsername);
        dto.setIsFavourite(isFavourite);

        return dto;
    }


    //СОХРАНЕНИЕ РЕЦЕПТА
    //NOT DONE
    @Transactional
    public RecipeDTO saveRecipe(CreateRecipeDTO createRecipeDTO, MultipartFile image, Principal principal) {
        log.info("Запрос на создание нового рецепта: {}", createRecipeDTO.getTitle());
        String imageName = null;
        Recipe savedRecipe;

        try {
            imageName = fileStorageService.storeFile(image);
            log.info("Изображение для рецепта {} успешно сохранено: {}", createRecipeDTO.getTitle(), imageName);

            Category category = categoryRepository.findById(createRecipeDTO.getCategoryId()).orElseThrow(() -> new EntityNotFoundException("Категория не найдена по id"));
            User user = userRepository.findByUsername(principal.getName()).orElseThrow(() -> new EntityNotFoundException("Пользователь не найден по ID"));

            Recipe recipe = recipeMapper.toRecipeEntity(createRecipeDTO, user, category, imageName);
            recipe.setIsConfirmed(false);
            savedRecipe = recipeRepository.save(recipe);
            log.info("Рецепт успешно создан: ID={}, Name={}", savedRecipe.getId(), savedRecipe.getTitle());

        } catch (SaveFileException e) {
            log.error("Не удалось сохранить картинку для рецепта: {}. Ошибка: {}", createRecipeDTO.getTitle(), e.getMessage());
            throw new SaveRecipeException("Failed to save recipe image: " + e.getMessage());
        } catch (Exception dbException) {
            if (imageName != null) {
                fileStorageService.deleteFile(imageName);
                log.warn("Откат: Файл {} удалён из-за ошибки транзакции БД.", imageName);
            }
            throw dbException;
        }
        return recipeMapper.toRecipeDto(savedRecipe);
    }

    public RecipeDTO deleteRecipe(Long id) {
        log.info("Запрос на удаление рецепта с ID: {}", id);
        Recipe recipe = recipeRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Рецепт с ID {} не найден для удаления", id);
                    return new EntityNotFoundException(ERROR_RECIPE_NOT_FOUND + id);
                });
        recipeRepository.deleteById(id);
        log.info("Рецепт с ID {} успешно удален: {}", id, recipe.getTitle());
        return recipeMapper.toRecipeDto(recipe);
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

}
