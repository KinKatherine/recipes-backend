package com.group.collectionOfRecipes.repositories;

import com.group.collectionOfRecipes.entities.Recipe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RecipeRepository extends JpaRepository<Recipe,Long> {
    List<Recipe> findByTitleContainingIgnoreCase(String title);
    List<Recipe> findRecipesByCategoryId(Long id);
}
