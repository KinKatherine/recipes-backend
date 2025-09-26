package com.group.collectionOfRecipes.mappers;

import com.group.collectionOfRecipes.dto.categoryDTO.CategoryDTO;
import com.group.collectionOfRecipes.dto.categoryDTO.CreateCategoryDTO;
import com.group.collectionOfRecipes.entities.Category;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CategoryMapper {


    public CategoryDTO toCategoryDto(Category category) {

        return CategoryDTO.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .build();
    }


    public Category toCategoryEntity(CreateCategoryDTO createCategoryDTO) {
        return Category.builder()
                .name(createCategoryDTO.getName())
                .description(createCategoryDTO.getDescription())
                .build();
    }
}
