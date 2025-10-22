package com.group.collectionofrecipes.controllers;


import com.group.collectionofrecipes.dto.ratingdto.CreateRatingDTO;
import com.group.collectionofrecipes.dto.ratingdto.RatingDTO;
import com.group.collectionofrecipes.dto.responsedto.ApiResponse;
import com.group.collectionofrecipes.services.RatingService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

import static com.group.collectionofrecipes.utils.ApiConstants.FIELD_RATING;
import static com.group.collectionofrecipes.utils.ApiConstants.FIELD_STATUS;
import static com.group.collectionofrecipes.utils.ApiConstants.FIELD_SUCCESS;
import static com.group.collectionofrecipes.utils.ApiConstants.FIELD_MESSAGE;

@Tag(name = "Ratings")
@Slf4j
@RestController
@RequiredArgsConstructor
public class RatingController {

    private final RatingService ratingService;


    // СОЗДАНИЕ РЕЙТИНГА
    @PostMapping("/api/v1/ratings")
    public ApiResponse<RatingDTO> createRating(@RequestBody @Valid CreateRatingDTO createRatingDTO,
                                    Principal principal) {

        if (principal == null) {
            log.warn("Попытка создать рейтинг без авторизации.");
            return ApiResponse.unSuccess(HttpStatus.UNAUTHORIZED);
        }

        RatingDTO newRatingDTO = ratingService.createRating(createRatingDTO, principal);
        if (newRatingDTO == null) {
            log.warn("Не удалось создать рейтинг: Пользователь или рецепт не найдены.");
            return ApiResponse.unSuccess(HttpStatus.NOT_FOUND);
        }
        return ApiResponse.success(newRatingDTO);
    }


    // УДАЛЕНИЕ РЕЙТИНГА
    @DeleteMapping("/api/v1/ratings/{recipeId}")
    public ApiResponse<RatingDTO> deleteRating(@PathVariable Long recipeId,
                                                           Principal principal) {

        if (principal == null) {
            return ApiResponse.unSuccess(HttpStatus.UNAUTHORIZED);
        }

        try {
            RatingDTO deletedRating = ratingService.deleteRating(recipeId, principal);
            return ApiResponse.success(deletedRating);

        } catch (EntityNotFoundException e) {
            log.warn("Удаление не удалось: {}", e.getMessage());
            return ApiResponse.unSuccess(HttpStatus.NOT_FOUND);
        }
    }


    //ОБНОВЛЕНИЕ РЕЙТИНГА
    @PutMapping("/api/v1/ratings/{recipeId}")
    public ApiResponse<RatingDTO> updateRating(@PathVariable Long recipeId,
                                                           @RequestParam Integer newEstimation,
                                                           Principal principal) {

        if (principal == null) {
            return ApiResponse.unSuccess(HttpStatus.UNAUTHORIZED);
        }

        try {
            RatingDTO updatedRatingDTO = ratingService.updateRating(recipeId, newEstimation, principal);
            return ApiResponse.success(updatedRatingDTO);

        } catch (EntityNotFoundException e) {
            log.warn("Обновление не удалось: {}", e.getMessage());
            return ApiResponse.unSuccess(HttpStatus.NOT_FOUND);
        }
    }
}