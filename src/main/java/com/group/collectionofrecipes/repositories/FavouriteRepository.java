package com.group.collectionofrecipes.repositories;

import com.group.collectionofrecipes.entities.Favourite;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FavouriteRepository extends JpaRepository<Favourite,Long> {

    boolean existsByRecipeIdAndUserUsername(Long recipeId, String username);

}
