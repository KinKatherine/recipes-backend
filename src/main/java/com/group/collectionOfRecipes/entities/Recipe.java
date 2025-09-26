package com.group.collectionOfRecipes.entities;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@Entity
@Table(name = "recipes")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Recipe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "title",nullable = false)
    private String title;

    @Column(name = "description", length = 1000)
    private String description;

    @Column(name = "instruction", columnDefinition = "TEXT")
    private String instruction;

    @Column(name = "cookingTime")
    private Integer cookingTime;

    @Column(name = "image")
    private String image;

    @Column(name = "servings_count")
    private Integer countOfServings;



    @Column(name = "total_rating")
    private Integer totalRating;

    @Column(name = "count_of_ratings")
    private Long countOfRatings;


    //много рецептов - один автор
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;                                  ///-FK

    //много рецептов - одна категория
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;                           ///-FK

    @Column(name = "createdAt")
    private LocalDateTime createdAt;

    //один рецепт нравится многим пользователям
    //многие ко многим через НРАВИТСЯ
    @OneToMany(mappedBy = "recipe", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Favourite> favoriteBy = new ArrayList<>();

    //один рецепт коммментирует много пользоватетлей
    //многие ко многим через КОММЕНТАРИИ
    @OneToMany(mappedBy = "recipe", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @Builder.Default
    private List<Comment> comments = new ArrayList<>();

    //один рецепт имеет много ингредиентов
    //многие ко многим через РЕЦЕПТ_ИНГРЕДИЕНТ
    @OneToMany(mappedBy = "recipe", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @Builder.Default
    private List<RecipeIngredientMapping> ingredientMappings = new ArrayList<>();

    @PrePersist
    protected void init() {
        createdAt = LocalDateTime.now();
    }


}