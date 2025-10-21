package com.group.collectionofrecipes.repositories;

import com.group.collectionofrecipes.entities.RecipeIngredientMapping;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecipeIngredientRepository extends JpaRepository<RecipeIngredientMapping,Long> {

}
