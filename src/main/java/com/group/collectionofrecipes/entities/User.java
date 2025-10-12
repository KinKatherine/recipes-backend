package com.group.collectionofrecipes.entities;

import com.group.collectionofrecipes.enums.UserRole;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "username", nullable = false, unique = true)
    private String username;

    @Column(name = "email", nullable = false, unique = true) //-это логин
    private String email;

    @Column(name = "password", nullable = false)
    private String password;


    @Column(name = "verification_token", length = 64)
    private String verificationToken;

    @Column(name = "enabled")
    private boolean enabled = false; // По умолчанию пользователь не активирован

    @Enumerated(EnumType.STRING)
    private UserRole role;

    @Column(name = "createdAt")
    private LocalDateTime createdAt;

    @Column(name = "photo")
    private String photo;


    //один пользователь - автор многих рецептов
    @OneToMany(mappedBy = "author", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Recipe> recipes = new ArrayList<>();     //-FK

    //одному пользователю нравится много рецептов
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Favourite> favourites = new ArrayList<>();

    //один пользователь - автор многих комментариев
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @Builder.Default
    private List<Comment> comments = new ArrayList<>();

    @PrePersist
    protected void init() {
        createdAt = LocalDateTime.now();
    }
}