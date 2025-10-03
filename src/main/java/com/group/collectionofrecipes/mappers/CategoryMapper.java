package com.group.collectionofrecipes.mappers;

import com.group.collectionofrecipes.dto.categorydto.CategoryDTO;
import com.group.collectionofrecipes.dto.categorydto.CreateCategoryDTO;
import com.group.collectionofrecipes.entities.Category;
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
