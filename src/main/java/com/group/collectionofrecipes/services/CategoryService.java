package com.group.collectionofrecipes.services;

import com.group.collectionofrecipes.dto.categorydto.CategoryDTO;
import com.group.collectionofrecipes.dto.categorydto.CreateCategoryDTO;
import com.group.collectionofrecipes.dto.recipedto.RecipeDTO;
import com.group.collectionofrecipes.entities.Category;
import com.group.collectionofrecipes.entities.Recipe;
import com.group.collectionofrecipes.mappers.CategoryMapper;
import com.group.collectionofrecipes.mappers.RecipeMapper;
import com.group.collectionofrecipes.repositories.CategoryRepository;
import com.group.collectionofrecipes.repositories.RecipeRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static com.group.collectionofrecipes.utils.ApiConstants.ERROR_CATEGORY_NOT_FOUND;

@Service
@Slf4j
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;
    private final RecipeRepository recipeRepository;
    private final RecipeMapper recipeMapper;

    public List<CategoryDTO> findAllCategories() {
        log.info("Запрос на получение всех категорий");
        List<Category> categoryList = categoryRepository.findAll();
        List<CategoryDTO> categoryDTOList = new ArrayList<>();
        for (Category i : categoryList) {
            categoryDTOList.add(categoryMapper.toCategoryDto(i));
        }
        log.info("Найдено {} категорий", categoryDTOList.size());
        return categoryDTOList;
    }

    public CategoryDTO findCategoryById(Long id) {
        log.info("Запрос на получение категории по ID: {}", id);
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Категория с ID {} не найдена", id);
                    return new EntityNotFoundException(ERROR_CATEGORY_NOT_FOUND + id);
                });
        log.info("Категория с ID {} успешно найдена: {}", id, category.getName());
        return categoryMapper.toCategoryDto(category);
    }


    public CategoryDTO saveCategory(CreateCategoryDTO createCategoryDTO) {
        log.info("Запрос на создание новой категории: {}", createCategoryDTO.getName());
        try {
            Category category = categoryMapper.toCategoryEntity(createCategoryDTO);
            Category savedCategory = categoryRepository.save(category);
            log.info("Категория успешно создана: ID={}, Name={}", savedCategory.getId(), savedCategory.getName());
            return categoryMapper.toCategoryDto(savedCategory);
        } catch (DataIntegrityViolationException e) {
            log.error("Ошибка при создании категории: категория '{}' уже существует", createCategoryDTO.getName());
            throw new IllegalArgumentException("Категория '" + createCategoryDTO.getName() + "' уже существует");
        }
    }


    public CategoryDTO deleteCategory(Long id) {
        log.info("Запрос на удаление категории с ID: {}", id);

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Категория с ID {} не найдена для удаления", id);
                    return new EntityNotFoundException(ERROR_CATEGORY_NOT_FOUND + id);
                });

        if (!category.getRecipes().isEmpty()) {
            log.warn("Попытка удаления категории с ID {}, содержащей {} рецептов",
                    id, category.getRecipes().size());
            throw new IllegalStateException("Cannot delete category with existing recipes");
        }

        categoryRepository.deleteById(id);
        log.info("Категория с ID {} успешно удалена: {}", id, category.getName());
        return categoryMapper.toCategoryDto(category);
    }


    public CategoryDTO updateCategory(Long id, CreateCategoryDTO createCategoryDTO) {
        log.info("Запрос на обновление категории с ID: {}, новые данные: {}",
                id, createCategoryDTO.getName());

        try {
            Category category = categoryRepository.findById(id)
                    .orElseThrow(() -> {
                        log.error("Категория с ID {} не найдена для обновления", id);
                        return new EntityNotFoundException("Category not found by id: " + id);
                    });

            category.setName(createCategoryDTO.getName());
            category.setDescription(createCategoryDTO.getDescription());

            Category updatedCategory = categoryRepository.save(category);
            log.info("Категория с ID {} успешно обновлена: {}", id, updatedCategory.getName());
            return categoryMapper.toCategoryDto(updatedCategory);
        } catch (DataIntegrityViolationException e) {
            log.error("Ошибка при обновлении категории: категория с названием '{}' уже существует",
                    createCategoryDTO.getName());
            throw new IllegalArgumentException("Категория с таким названием уже существует");
        } catch (Exception e) {
            log.error("Неожиданная ошибка при обновлении категории с ID {}: {}", id, e.getMessage());
            throw e;
        }
    }
}