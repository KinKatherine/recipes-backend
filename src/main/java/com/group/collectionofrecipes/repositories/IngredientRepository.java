package com.group.collectionofrecipes.repositories;

import com.group.collectionofrecipes.entities.Ingredient;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IngredientRepository extends JpaRepository<Ingredient, Long> {

}
