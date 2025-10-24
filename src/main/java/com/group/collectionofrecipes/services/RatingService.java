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

import static com.group.collectionofrecipes.utils.ApiConstants.ERROR_RECIPE_NOT_FOUND;
import static com.group.collectionofrecipes.utils.ApiConstants.ERROR_USER_NOT_FOUND;

@Service
@Slf4j
@RequiredArgsConstructor
public class RatingService {

    private final UserRepository userRepository;
    private final RecipeRepository recipeRepository;
    private final RatingMapper ratingMapper;
    private final RatingRepository ratingRepository;

    @Transactional
    public RatingDTO createRating(CreateRatingDTO createRatingDTO, Principal principal) {

        log.info("Запрос на оценку рецепта с id {} пользователем {}", createRatingDTO.getRecipeId(), principal.getName());
        String username = principal.getName();
        Long recipeId = createRatingDTO.getRecipeId();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.warn("Пользователь {} не найден при попытке создания оценки.", username);
                    return new EntityNotFoundException(ERROR_USER_NOT_FOUND + username);
                });

        Recipe recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> {
                    log.warn("Рецепт с id {} не найден при попытке создания оценки.", createRatingDTO.getRecipeId());
                    return new EntityNotFoundException(ERROR_RECIPE_NOT_FOUND + recipeId);
                });


        boolean existingRating = ratingRepository.existsByRecipeIdAndUserId(recipeId, user.getId());

        if (existingRating) {
            log.warn("Пользователь {} уже оценил рецепт ID {}. Создание дублирующего рейтинга запрещено.", username, recipeId);
            throw new IllegalStateException("Вы уже поставили рейтинг этому рецепту. Используйте PUT для обновления.");
        }

        Rating newRating = ratingMapper.toRatingEntity(createRatingDTO, user, recipe);
        Rating savedRating = ratingRepository.save(newRating);
        log.info("Рейтинг ID {} успешно создан пользователем {} для рецепта ID {}.", savedRating.getId(), username, recipeId);

        return ratingMapper.toRatingDto(savedRating);
    }

    @Transactional
    public RatingDTO deleteRating(Long recipeId, Principal principal) {

        log.info("Запрос на удаление оценки рецепта с id {} пользователем {}", recipeId, principal.getName());
        String username = principal.getName();
        Rating ratingToDelete = ratingRepository.findByRecipeIdAndUserUsername(recipeId, principal.getName())
                .orElseThrow(() -> {
                    log.warn("Рейтинг для рецепта ID {} от пользователя {} не найден.", recipeId, username);
                    return new EntityNotFoundException("Рейтинг для рецепта от пользователя не найден.");
                });

        Long ratingId = ratingToDelete.getId();
        ratingRepository.delete(ratingToDelete);

        log.info("Рейтинг ID {} успешно удален пользователем {} для рецепта ID {}.", ratingId, username, recipeId);

        return ratingMapper.toRatingDto(ratingToDelete);
    }

    @Transactional
    public RatingDTO updateRating(Long recipeId, Integer estimation, Principal principal) {

        String username = principal.getName();
        Rating existingRating = ratingRepository.findByRecipeIdAndUserUsername(recipeId, principal.getName())
                .orElseThrow(() -> {
                    log.warn("Рейтинг для рецепта ID {} от пользователя {} не найден. Невозможно обновить.", recipeId, username);
                    return new EntityNotFoundException("Рейтинг для рецепта ID  от пользователя не найден. Невозможно обновить.");
                });

        Long ratingId = existingRating.getId();
        existingRating.setEstimation(estimation);
        Rating updatedRating = ratingRepository.save(existingRating);
        log.info("Обновление рейтинга ID {} от пользователя {}: оценка изменена с {} на {}.", ratingId, username, existingRating.getEstimation(), estimation);
        return ratingMapper.toRatingDto(updatedRating);
    }


}