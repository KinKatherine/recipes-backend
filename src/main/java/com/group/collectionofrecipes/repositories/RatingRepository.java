package com.group.collectionofrecipes.repositories;

import com.group.collectionofrecipes.entities.Rating;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RatingRepository extends JpaRepository<Rating,Long> {

    boolean existsByRecipeIdAndUserUsername(Long recipeId, String username); // метод для проверки оценивал ли пользовватель рецепт

    Optional<Rating> findByRecipeIdAndUserId(Long recipeId, Long userId);

    Optional<Rating> findByRecipeIdAndUsername(Long recipeId, String username);
}
