package com.group.collectionofrecipes.repositories;

import com.group.collectionofrecipes.entities.Ingredient;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Set;


public interface IngredientRepository extends JpaRepository<Ingredient, Long> {

    List<Ingredient> findAllByNameIn(Set<String> formattedNames);
}
