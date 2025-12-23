package com.group.collectionofrecipes.repositories;

import com.group.collectionofrecipes.dto.ratingdto.RatingStatsProjection;
import com.group.collectionofrecipes.dto.ratingdto.RecipeRatingProjection;
import com.group.collectionofrecipes.entities.Comment;
import com.group.collectionofrecipes.entities.Recipe;
import com.group.collectionofrecipes.entities.RecipeIngredientMapping;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RecipeRepository extends JpaRepository<Recipe, Long> {


    // ДЛЯ ОДНОГО РЕЦЕПТА
    @Query("SELECT r FROM Recipe r " +
            "JOIN FETCH r.author " +
            "JOIN FETCH r.category " +
            "WHERE r.id = :id AND r.isConfirmed = true")
    Optional<Recipe> findByIdWithMainDetails(@Param("id") Long id); // возвращаем рецепт с категорией и автором

    @Query("SELECT COALESCE(AVG(r.estimation), 0) as averageRating, COUNT(r.estimation) as count " +
            "FROM Rating r WHERE r.recipe.id = :recipeId")
    RatingStatsProjection calculateRatingStats(Long recipeId);

    @Query("SELECT c FROM Comment c JOIN FETCH c.user WHERE c.recipe.id = :recipeId ORDER BY c.createdAt ASC")
    List<Comment> loadCommentsByRecipeId(@Param("recipeId") Long recipeId); //загружаем комментарии рецепта по id

    @Query("SELECT im FROM RecipeIngredientMapping im JOIN FETCH im.ingredient WHERE im.recipe.id = :recipeId")
    List<RecipeIngredientMapping> loadIngredientMappersByRecipeId(@Param("recipeId") Long recipeId); //загружаем ингредиенты рецепта по id


    //ДЛЯ РЕЦЕПТА ДНЯ
    @Query(value = "SELECT * FROM recipes WHERE is_confirmed = true ORDER BY MD5(CONCAT(:seed, id)) LIMIT 1", nativeQuery = true)
    Optional<Recipe> findRandomRecipeWithSeed(String seed); //генерим рандомный рецепт дня


    //ДЛЯ 3 ПОСЛЕДНИХ ДОБАВЛЕННЫХ РЕЦЕПТОВ
    @Query("SELECT r FROM Recipe r " +
            "JOIN FETCH r.author " +
            "JOIN FETCH r.category " +
            "WHERE r.isConfirmed = true " +
            "ORDER BY r.id DESC")
    List<Recipe> findLatestRecipes(Pageable pageable); // страница с 3мя последними рецептами

    @Query("SELECT r.id AS id, COALESCE(AVG(rating.estimation), 0.0) AS averageRating " +
            "FROM Recipe r LEFT JOIN Rating rating ON r.id = rating.recipe.id " +
            "WHERE r.id IN :recipeIds GROUP BY r.id")
    List<RecipeRatingProjection> findAverageRatingsForRecipes(@Param("recipeIds") List<Long> recipeIds);


    //ДЛЯ ИЗБРАННЫХ РЕЦЕПТОВ ПОЛЬЗОВАТЕЛЯ
    @EntityGraph(attributePaths = {"author", "category"})
    @Query("SELECT r FROM Recipe r " +
            "JOIN r.favoriteBy fb " +
            "WHERE r.isConfirmed = true " +
            "AND fb.user.username = :username " +
            "ORDER BY fb.addedAt DESC")
    Page<Recipe> findConfirmedFavoriteRecipesByUsername(@Param("username") String username, Pageable pageable);


    //ДЛЯ ОБОБРЕННЫХ РЕЦЕПТОВ ПОЛЬЗОВАТЕЛЯ
    @Query("SELECT r FROM Recipe r " +
            "JOIN FETCH r.author " +
            "JOIN FETCH r.category " +
            "WHERE r.isConfirmed = true " +
            "AND r.author.username = :username ")
    Page<Recipe> findConfirmedRecipesByAuthorUsername(@Param("username") String username, Pageable pageable);

    //ДЛЯ НЕОБОДРЕННЫХ РЕЦЕПТОВ ПОЛЬЗОВАТЕЛЕЙ
    @Query("SELECT r FROM Recipe r " +
            "JOIN FETCH r.author " +
            "JOIN FETCH r.category " +
            "WHERE r.isConfirmed = false ")
    Page<Recipe> findUnconfirmedRecipes(Pageable pageable);

    //ПОИСК РЕЦЕПТОВ ОПРЕДЕЛЕННОЙ КАТЕГОРИИ
    @Query("SELECT r FROM Recipe r " +
            "JOIN FETCH r.author " +
            "JOIN FETCH r.category " +
            "WHERE r.category.id = :categoryId " +
            "AND r.isConfirmed = true")
    Page<Recipe> findRecipesByCategoryId(Long categoryId, Pageable pageable);


    //СТРАНИЦА ПОЛУЛЯРНОЕ ГЛАВНОЙ СТРАНИЦЫ
    @Query("SELECT r FROM Recipe r " +
            "LEFT JOIN r.ratings rat " +
            "WHERE r.isConfirmed = true " +
            "GROUP BY r " +
            "ORDER BY COALESCE(AVG(rat.estimation), 0) DESC, COUNT(rat.id) DESC")
    List<Recipe> findTop10ByRatingAndVotesCount();


    //ПОИСК РЕЦЕПТА ПО ВХОЖНЕИЮ НАЗВАНИЯ
    @Query("SELECT r FROM Recipe r " +
            "JOIN FETCH r.author " +
            "JOIN FETCH r.category " +
            "LEFT JOIN FETCH r.favoriteBy " +
            "WHERE r.isConfirmed = true " +
            "AND LOWER(r.title) LIKE LOWER(CONCAT('%', :title, '%'))")
    List<Recipe> findByTitleContainingIgnoreCase(String title);

    @Query("SELECT r FROM Recipe r " +
            "JOIN FETCH r.author " +
            "JOIN FETCH r.category " +
            "WHERE r.isConfirmed = true")
    Page<Recipe> sortRecipesByCookingTime(Pageable pageRequest);

    // РАСШИРЕННЫЙ ПОИСК
    @Query("SELECT r FROM Recipe r " +
            "JOIN FETCH r.author " +
            "JOIN FETCH r.category " +
            "WHERE r.isConfirmed = true " +
            "AND (:query IS NULL OR (" +
            "   LOWER(r.title) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "   LOWER(r.description) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "   LOWER(r.instruction) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "   EXISTS (SELECT 1 FROM RecipeIngredientMapping rim JOIN rim.ingredient i WHERE rim.recipe = r AND LOWER(i.name) " +
            " LIKE LOWER(CONCAT('%', :query, '%')))" +
            ")) " +
            "AND (:categoryId IS NULL OR r.category.id = :categoryId) " +
            "AND (:authorId IS NULL OR r.author.id = :authorId) " +
            "AND (:startDate IS NULL OR r.createdAt >= :startDate) " +
            "AND (:endDate IS NULL OR r.createdAt <= :endDate)")
    Page<Recipe> searchRecipes(@Param("query") String query,
                               @Param("categoryId") Long categoryId,
                               @Param("authorId") Long authorId,
                               Pageable pageable);
}
