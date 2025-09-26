package com.group.collectionOfRecipes.services;

import com.group.collectionOfRecipes.dto.categoryDTO.CategoryDTO;
import com.group.collectionOfRecipes.dto.categoryDTO.CreateCategoryDTO;
import com.group.collectionOfRecipes.dto.recipeDTO.RecipeDTO;
import com.group.collectionOfRecipes.entities.Category;
import com.group.collectionOfRecipes.entities.Recipe;
import com.group.collectionOfRecipes.mappers.CategoryMapper;
import com.group.collectionOfRecipes.mappers.RecipeMapper;
import com.group.collectionOfRecipes.repositories.CategoryRepository;
import com.group.collectionOfRecipes.repositories.RecipeRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;
    private final RecipeRepository recipeRepository;
    private final RecipeMapper recipeMapper;

    public List<CategoryDTO> findAllCategories() {
        List<Category> categoryList = categoryRepository.findAll();
        List<CategoryDTO> categoryDTOList = new ArrayList<>();
        for (Category i:categoryList)
        {
            categoryDTOList.add(categoryMapper.toCategoryDto(i));
        }
        return categoryDTOList;
    }

    public CategoryDTO findCategoryById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Category not found by id: " + id));
        return categoryMapper.toCategoryDto(category);
    }

    public CategoryDTO saveCategory(CreateCategoryDTO createCategoryDTO) {

        try {
            Category category = categoryMapper.toCategoryEntity(createCategoryDTO);
            return categoryMapper.toCategoryDto(categoryRepository.save(category));
        }
        catch (DataIntegrityViolationException e)
        {
            throw new IllegalArgumentException("Такая категория уже существует");
        }
    }

    public CategoryDTO deleteCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Category not found by id: " + id));

        if (!category.getRecipes().isEmpty()) {
            throw new IllegalStateException("Cannot delete category with existing recipes");
        }

        categoryRepository.deleteById(id);
        return categoryMapper.toCategoryDto(category);
    }


    public CategoryDTO updateCategory(Long id, CreateCategoryDTO createCategoryDTO) {
        try {
            Category category = categoryRepository.findById(id)
                    .orElseThrow(() -> new EntityNotFoundException("Category not found by id: " + id));

            category.setName(createCategoryDTO.getName());
            category.setDescription(createCategoryDTO.getDescription());
            return categoryMapper.toCategoryDto(categoryRepository.save(category));
        } catch (DataIntegrityViolationException e) {
            throw new IllegalArgumentException("Категория с таким названием уже существует");
        }
    }

    public List<RecipeDTO> getRecipesByCategoryId(Long id) {

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Category not found by id: " + id));

        List<RecipeDTO> recipeDTOList = new ArrayList<>();
        for (Recipe i: recipeRepository.findRecipesByCategoryId(id))
        {
            recipeDTOList.add(recipeMapper.toRecipeDto(i));
        }
        return recipeDTOList;
    }
}
