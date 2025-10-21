package com.group.collectionofrecipes.services;

import com.group.collectionofrecipes.dto.ratingdto.CreateRatingDTO;
import com.group.collectionofrecipes.dto.ratingdto.RatingDTO;
import com.group.collectionofrecipes.entities.Rating;
import com.group.collectionofrecipes.entities.Recipe;
import com.group.collectionofrecipes.entities.User;
import com.group.collectionofrecipes.mappers.RatingMapper;
import com.group.collectionofrecipes.repositories.RatingRepository;
import com.group.collectionofrecipes.repositories.RecipeRepository;
import com.group.collectionofrecipes.repositories.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;

import static com.group.collectionofrecipes.utils.ApiConstants.ERROR_USER_NOT_FOUND;

@Service
@Slf4j
@RequiredArgsConstructor
public class RatingService{

    private final UserRepository userRepository;
    private final RecipeRepository recipeRepository;
    private final RatingMapper ratingMapper;
    private final RatingRepository ratingRepository;

    @Transactional
    public RatingDTO createRating(CreateRatingDTO createRatingDTO, Principal principal) {

        String username = principal.getName();
        Long recipeId = createRatingDTO.getRecipeId();

        try {
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new EntityNotFoundException(ERROR_USER_NOT_FOUND + username));

            Recipe recipe = recipeRepository.findById(recipeId)
                    .orElseThrow(() -> new EntityNotFoundException("Рецепт не найден по id: " + recipeId));

            Rating newRating = ratingMapper.toRatingEntity(createRatingDTO, user, recipe);
            Rating savedRating = ratingRepository.save(newRating);
            log.info("Рейтинг ID {} успешно создан пользователем {} для рецепта ID {}.", savedRating.getId(), username, recipeId);

            return ratingMapper.toRatingDto(savedRating);

        } catch (EntityNotFoundException e) {
            log.error("Не удалось создать рейтинг для рецепта ID {}. Ошибка: {}", recipeId, e.getMessage(), e);
            return null;
        }
    }

    @Transactional
    public Long deleteRating(Long recipeId, Principal principal) {

        String username = principal.getName();

        try {
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new EntityNotFoundException(ERROR_USER_NOT_FOUND + username));

            Rating ratingToDelete = ratingRepository.findByRecipeIdAndUserId(recipeId, user.getId())
                    .orElseThrow(() -> new EntityNotFoundException("Рейтинг для рецепта ID " + recipeId + " от пользователя " + username + " не найден."));

            Long ratingId = ratingToDelete.getId();
            ratingRepository.delete(ratingToDelete);

            log.info("Рейтинг ID {} успешно удален пользователем {} для рецепта ID {}.", ratingId, username, recipeId);

            return ratingId;

        } catch (EntityNotFoundException e) {
            log.error("Не удалось удалить рейтинг для рецепта ID {}. Ошибка: {}", recipeId, e.getMessage(), e);
            throw e;
        }
    }

    @Transactional
    public RatingDTO updateRating(Long recipeId, Integer estimation, Principal principal){

        String username = principal.getName();

        try {
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new EntityNotFoundException(ERROR_USER_NOT_FOUND + username));

            Rating existingRating = ratingRepository.findByRecipeIdAndUserId(recipeId, user.getId())
                    .orElseThrow(() -> new EntityNotFoundException("Рейтинг для рецепта ID " + recipeId + " от пользователя " + username + " не найден. Невозможно обновить."));

            Long ratingId = existingRating.getId();
            log.info("Обновление рейтинга ID {} от пользователя {}: оценка изменена с {} на {}.", ratingId, username, existingRating.getEstimation(), estimation);
            existingRating.setEstimation(estimation);
            Rating updatedRating = ratingRepository.save(existingRating);
            return ratingMapper.toRatingDto(updatedRating);

        } catch (EntityNotFoundException e) {
            log.error("Не удалось обновить рейтинг для рецепта ID {}. Ошибка: {}", recipeId, e.getMessage(), e);
            throw e;
        }
    }

}