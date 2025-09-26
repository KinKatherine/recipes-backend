package com.group.collectionOfRecipes.entities;

import com.group.collectionOfRecipes.enums.Unit;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "ingredients")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Ingredient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "name", unique = true, nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    private Unit unit;

    @Column(name = "createdAt")
    private LocalDateTime createdAt;

    //один ингредиент может быть во многих рецептах
    @OneToMany(mappedBy = "ingredient", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<RecipeIngredientMapping> recipeMappings = new ArrayList<>();

    @PrePersist
    protected void init() {
        createdAt = LocalDateTime.now();
    }
}
