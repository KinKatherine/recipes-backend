package com.group.collectionofrecipes.services;

import com.group.collectionofrecipes.dto.recipedto.CreateRecipeDTO;
import com.group.collectionofrecipes.dto.recipedto.RecipeDTO;
import com.group.collectionofrecipes.entities.Category;
import com.group.collectionofrecipes.entities.Recipe;
import com.group.collectionofrecipes.entities.User;
import com.group.collectionofrecipes.exceptions.SaveFileException;
import com.group.collectionofrecipes.exceptions.SaveRecipeException;
import com.group.collectionofrecipes.mappers.RecipeMapper;
import com.group.collectionofrecipes.repositories.CategoryRepository;
import com.group.collectionofrecipes.repositories.RecipeRepository;
import com.group.collectionofrecipes.repositories.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

import static com.group.collectionofrecipes.utils.ApiConstants.ERROR_RECIPE_NOT_FOUND;

@Service
@Slf4j
@RequiredArgsConstructor
public class RecipeService {

    private final LocalFileStorageService fileStorageService;
    private final RecipeRepository recipeRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final RecipeMapper recipeMapper;

    public List<RecipeDTO> findAllRecipes(String title) {
        log.info("Запрос на получение всех рецептов");
        List<Recipe> recipeList = (title != null ? recipeRepository.findByTitleContainingIgnoreCase(title) : recipeRepository.findAll());
        List<RecipeDTO> recipeDTOList = new ArrayList<>();
        for (Recipe i : recipeList) {
            recipeDTOList.add(recipeMapper.toRecipeDto(i));
        }
        log.info("Найдено {} рецептов", recipeDTOList.size());
        return recipeDTOList;
    }

    public RecipeDTO findRecipeById(Long id) {
        log.info("Запрос на получение рецепта по ID: {}", id);
        Recipe recipe = recipeRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Рецепт с ID {} не найден", id);
                    return new EntityNotFoundException(ERROR_RECIPE_NOT_FOUND + id);
                });
        log.info("Рецепт с ID {} успешно найден: {}", id, recipe.getTitle());
        return recipeMapper.toRecipeDto(recipe);
    }

    public RecipeDTO saveRecipe(CreateRecipeDTO createRecipeDTO, MultipartFile image, Principal principal) {
        log.info("Запрос на создание нового рецепта: {}", createRecipeDTO.getTitle());
        String imageName;
        Recipe savedRecipe;

        try {
            imageName = fileStorageService.storeFile(image);
            log.info("Изображение для рецепта {} успешно сохранено: {}", createRecipeDTO.getTitle(), imageName);

            Category category = categoryRepository.findById(createRecipeDTO.getCategoryId())
                    .orElseThrow(() -> {
                        log.error("Категория с ID {} не найдена", createRecipeDTO.getCategoryId());
                        return new EntityNotFoundException("Category not found by id: " + createRecipeDTO.getCategoryId());
                    });

            User user = userRepository.findByUsername(principal.getName())
                    .orElseThrow(() -> {
                        log.error("Пользователь с именем {} не найден", principal.getName());
                        return new EntityNotFoundException("User not found by username: " + principal.getName());
                    });

            Recipe recipe = recipeMapper.toRecipeEntity(createRecipeDTO, user, category, imageName);
            savedRecipe = recipeRepository.save(recipe);
            log.info("Рецепт успешно создан: ID={}, Name={}", savedRecipe.getId(), savedRecipe.getTitle());

        } catch (SaveFileException e) {
            log.error("Не удалось сохранить картинку для рецепта: {}. Ошибка: {}", createRecipeDTO.getTitle(), e.getMessage());
            throw new SaveRecipeException("Failed to save recipe image: " + e.getMessage());
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
