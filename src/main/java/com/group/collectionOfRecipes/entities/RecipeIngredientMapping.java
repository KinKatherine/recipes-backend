package com.group.collectionOfRecipes.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "recipes_ingredients",
        uniqueConstraints = @UniqueConstraint(columnNames = {"ingredient_id", "recipe_id"}))
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecipeIngredientMapping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    //у одного рецепта много ингредиентов, один ингредиент входит во много рецептов
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ingredient_id", nullable = false)
    private Ingredient ingredient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipe_id", nullable = false)
    private Recipe recipe;

    @Column(name = "amount")
    private Double amount ;

}
