package com.group.collectionofrecipes.services;

import com.group.collectionofrecipes.dto.ratingdto.CreateRatingDTO;
import com.group.collectionofrecipes.dto.ratingdto.RatingDTO;
import com.group.collectionofrecipes.entities.Rating;
import com.group.collectionofrecipes.entities.Recipe;
import com.group.collectionofrecipes.entities.User;
import com.group.collectionofrecipes.exceptions.UnauthorizedUserException;
import com.group.collectionofrecipes.mappers.RatingMapper;
import com.group.collectionofrecipes.repositories.RatingRepository;
import com.group.collectionofrecipes.repositories.RecipeRepository;
import com.group.collectionofrecipes.repositories.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.security.Principal;
import java.util.Optional;

import static com.group.collectionofrecipes.utils.ApiConstants.UNAUTHORIZED_USER;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RatingServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private RecipeRepository recipeRepository;
    @Mock
    private RatingMapper ratingMapper;
    @Mock
    private RatingRepository ratingRepository;

    @Mock
    private Principal principal;

    @InjectMocks
    private RatingService ratingService;

    private final String username = "testUser";
    private final Long userId = 1L;
    private final Long recipeId = 10L;
    private final Long ratingId = 100L;
    private final Integer estimation = 5;
    private final Integer newEstimation = 4;

    private User user;
    private Recipe recipe;
    private Rating rating;
    private RatingDTO ratingDTO;
    private CreateRatingDTO createRatingDTO;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(userId);
        user.setUsername(username);

        recipe = new Recipe();
        recipe.setId(recipeId);

        rating = new Rating();
        rating.setId(ratingId);
        rating.setEstimation(estimation);
        rating.setUser(user);
        rating.setRecipe(recipe);

        ratingDTO = new RatingDTO(ratingId, userId, recipeId);

        createRatingDTO = new CreateRatingDTO();
        createRatingDTO.setRecipeId(recipeId);
        createRatingDTO.setEstimation(estimation);

        Mockito.lenient().when(principal.getName()).thenReturn(username);
    }

    @Test
    @DisplayName("Успешное создание рейтинга")
    void createRating_Success() {
        // Arrange
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(recipeRepository.findById(recipeId)).thenReturn(Optional.of(recipe));
        when(ratingRepository.existsByRecipeIdAndUserId(recipeId, userId)).thenReturn(false);
        when(ratingMapper.toRatingEntity(any(CreateRatingDTO.class), any(User.class), any(Recipe.class))).thenReturn(rating);
        when(ratingRepository.save(any(Rating.class))).thenReturn(rating);
        when(ratingMapper.toRatingDto(any(Rating.class))).thenReturn(ratingDTO);

        // Act
        RatingDTO result = ratingService.createRating(createRatingDTO, principal);

        // Assert
        assertNotNull(result);
        assertEquals(ratingId, result.getId());
        assertEquals(recipeId, result.getRecipeId());
        assertEquals(userId, result.getUserId());

        // Verify
        verify(userRepository).findByUsername(username);
        verify(recipeRepository).findById(recipeId);
        verify(ratingRepository).existsByRecipeIdAndUserId(recipeId, userId);
        verify(ratingRepository).save(any(Rating.class));
    }

    @Test
    @DisplayName("createRating: Ошибка - Principal == null")
    void createRating_PrincipalIsNull_ThrowsUnauthorizedUserException() {
        // Act & Assert
        UnauthorizedUserException exception = assertThrows(UnauthorizedUserException.class,
                () -> ratingService.createRating(createRatingDTO, null));
        assertEquals(UNAUTHORIZED_USER, exception.getMessage());
        verifyNoInteractions(userRepository, recipeRepository, ratingRepository, ratingMapper);
    }

    @Test
    @DisplayName("createRating: Ошибка - Пользователь не найден")
    void createRating_UserNotFound_ThrowsEntityNotFoundException() {
        // Arrange
        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class,
                () -> ratingService.createRating(createRatingDTO, principal));

        // Verify
        verify(userRepository).findByUsername(username);
        verifyNoMoreInteractions(recipeRepository, ratingRepository, ratingMapper);
    }

    @Test
    @DisplayName("createRating: Ошибка - Рецепт не найден")
    void createRating_RecipeNotFound_ThrowsEntityNotFoundException() {
        // Arrange
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(recipeRepository.findById(recipeId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class,
                () -> ratingService.createRating(createRatingDTO, principal));

        // Verify
        verify(userRepository).findByUsername(username);
        verify(recipeRepository).findById(recipeId);
        verifyNoMoreInteractions(ratingRepository, ratingMapper);
    }

    @Test
    @DisplayName("createRating: Ошибка - Рейтинг уже существует")
    void createRating_RatingAlreadyExists_ThrowsIllegalStateException() {
        // Arrange
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(recipeRepository.findById(recipeId)).thenReturn(Optional.of(recipe));
        when(ratingRepository.existsByRecipeIdAndUserId(recipeId, userId)).thenReturn(true);

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> ratingService.createRating(createRatingDTO, principal));

        assertEquals("Вы уже поставили рейтинг этому рецепту. Используйте PUT для обновления.", exception.getMessage());

        // Verify
        verify(userRepository).findByUsername(username);
        verify(recipeRepository).findById(recipeId);
        verify(ratingRepository).existsByRecipeIdAndUserId(recipeId, userId);
        verify(ratingRepository, never()).save(any());
        verifyNoInteractions(ratingMapper);
    }


    @Test
    @DisplayName("Успешное удаление рейтинга")
    void deleteRating_Success() {
        // Arrange
        when(ratingRepository.findByRecipeIdAndUserUsername(recipeId, username)).thenReturn(Optional.of(rating));
        when(ratingMapper.toRatingDto(any(Rating.class))).thenReturn(ratingDTO);

        // Act
        RatingDTO result = ratingService.deleteRating(recipeId, principal);

        // Assert
        assertNotNull(result);
        assertEquals(ratingId, result.getId());

        // Verify
        verify(ratingRepository).findByRecipeIdAndUserUsername(recipeId, username);
        verify(ratingRepository).delete(rating);
        verify(ratingMapper).toRatingDto(rating);
    }

    @Test
    @DisplayName("deleteRating: Ошибка - Principal == null")
    void deleteRating_PrincipalIsNull_ThrowsUnauthorizedUserException() {
        // Act & Assert
        UnauthorizedUserException exception = assertThrows(UnauthorizedUserException.class,
                () -> ratingService.deleteRating(recipeId, null));
        assertEquals(UNAUTHORIZED_USER, exception.getMessage());
        verifyNoInteractions(ratingRepository, ratingMapper);
    }

    @Test
    @DisplayName("deleteRating: Ошибка - Рейтинг не найден")
    void deleteRating_RatingNotFound_ThrowsEntityNotFoundException() {
        // Arrange
        when(ratingRepository.findByRecipeIdAndUserUsername(recipeId, username)).thenReturn(Optional.empty());

        // Act & Assert
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> ratingService.deleteRating(recipeId, principal));

        assertEquals("Рейтинг для рецепта от пользователя не найден.", exception.getMessage());

        // Verify
        verify(ratingRepository).findByRecipeIdAndUserUsername(recipeId, username);
        verify(ratingRepository, never()).delete(any());
        verifyNoInteractions(ratingMapper);
    }

    @Test
    @DisplayName("Успешное обновление рейтинга")
    void updateRating_Success() {
        // Arrange
        Rating updatedRatingEntity = new Rating();
        updatedRatingEntity.setId(ratingId);
        updatedRatingEntity.setEstimation(newEstimation);
        updatedRatingEntity.setUser(user);
        updatedRatingEntity.setRecipe(recipe);

        RatingDTO updatedRatingDTO = new RatingDTO(ratingId, userId, recipeId);

        when(ratingRepository.findByRecipeIdAndUserUsername(recipeId, username)).thenReturn(Optional.of(rating));
        when(ratingRepository.save(any(Rating.class))).thenReturn(updatedRatingEntity);
        when(ratingMapper.toRatingDto(any(Rating.class))).thenReturn(updatedRatingDTO);

        // Act
        RatingDTO result = ratingService.updateRating(recipeId, newEstimation, principal);

        // Assert
        assertNotNull(result);
        assertEquals(ratingId, result.getId());

        assertEquals(newEstimation, rating.getEstimation(), "Оценка в сущности должна быть обновлена.");

        // Verify
        verify(ratingRepository).findByRecipeIdAndUserUsername(recipeId, username);
        verify(ratingRepository).save(rating);
        verify(ratingMapper).toRatingDto(updatedRatingEntity);
    }

    @Test
    @DisplayName("updateRating: Ошибка - Principal == null")
    void updateRating_PrincipalIsNull_ThrowsUnauthorizedUserException() {
        // Act & Assert
        UnauthorizedUserException exception = assertThrows(UnauthorizedUserException.class,
                () -> ratingService.updateRating(recipeId, estimation, null));
        assertEquals(UNAUTHORIZED_USER, exception.getMessage());
        verifyNoInteractions(ratingRepository, ratingMapper);
    }

    @Test
    @DisplayName("updateRating: Ошибка - Рейтинг для обновления не найден")
    void updateRating_RatingNotFound_ThrowsEntityNotFoundException() {
        // Arrange
        when(ratingRepository.findByRecipeIdAndUserUsername(recipeId, username)).thenReturn(Optional.empty());

        // Act & Assert
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> ratingService.updateRating(recipeId, estimation, principal));

        assertEquals("Рейтинг для рецепта ID  от пользователя не найден. Невозможно обновить.", exception.getMessage());

        // Verify
        verify(ratingRepository).findByRecipeIdAndUserUsername(recipeId, username);
        verify(ratingRepository, never()).save(any());
        verifyNoInteractions(ratingMapper);
    }
}