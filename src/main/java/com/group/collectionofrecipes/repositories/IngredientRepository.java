package com.group.collectionofrecipes.repositories;

import com.group.collectionofrecipes.entities.Ingredient;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface IngredientRepository extends JpaRepository<Ingredient, Long> {


    List<Ingredient> findByNameContainingIgnoreCase(String name);
}
