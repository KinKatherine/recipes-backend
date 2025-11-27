package com.group.collectionofrecipes.controllers;


import com.group.collectionofrecipes.dto.ratingdto.CreateRatingDTO;
import com.group.collectionofrecipes.dto.ratingdto.RatingDTO;
import com.group.collectionofrecipes.dto.responsedto.ApiResponse;
import com.group.collectionofrecipes.exceptions.UnauthorizedUserException;
import com.group.collectionofrecipes.services.RatingService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

import static com.group.collectionofrecipes.utils.ApiConstants.FIELD_ERROR;
import static com.group.collectionofrecipes.utils.ApiConstants.FIELD_MESSAGE;
import static com.group.collectionofrecipes.utils.ApiConstants.FIELD_STATUS;

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
        log.info("GET /api/v1/ratings");
        if (principal == null) {
            log.warn("Попытка добавить рейтинг без авторизации.");
            throw new AccessDeniedException("Недостаточно прав");
        }
        RatingDTO newRatingDTO = ratingService.createRating(createRatingDTO, principal);
        log.info("GET /api/v1/ratings - оценка с id {} успешно создана", newRatingDTO.getId());
        return ApiResponse.success(newRatingDTO);
    }


    // УДАЛЕНИЕ РЕЙТИНГА
    @DeleteMapping("/api/v1/ratings/{recipeId}")
    public ApiResponse<RatingDTO> deleteRating(@PathVariable Long recipeId,
                                               Principal principal) {

        log.info("DELETE /api/v1/ratings/{}", recipeId);
        if (principal == null) {
            log.warn("Попытка удалить рейтинг без авторизации.");
            throw new AccessDeniedException("Недостаточно прав");
        }
        RatingDTO deletedRating = ratingService.deleteRating(recipeId, principal);
        log.info("DELETE /api/v1/ratings/{} - оценка с id {} успешно удалена", recipeId, deletedRating.getId());
        return ApiResponse.success(deletedRating);
    }


    //ОБНОВЛЕНИЕ РЕЙТИНГА
    @PutMapping("/api/v1/ratings/{recipeId}")
    public ApiResponse<RatingDTO> updateRating(@PathVariable Long recipeId,
                                               @RequestParam Integer newEstimation,
                                               Principal principal) {

        log.info("PUT /api/v1/ratings/{}", recipeId);
        if (newEstimation <= 0 || newEstimation > 5) {
            throw new IllegalArgumentException("Рейтинг должен быть от 1 до 5");
        }

        if (principal == null) {
            log.warn("Попытка обновить рейтинг без авторизации.");
            throw new AccessDeniedException("Недостаточно прав");
        }
        RatingDTO updatedRatingDTO = ratingService.updateRating(recipeId, newEstimation, principal);
        log.info("PUT /api/v1/ratings/{} - оценка с id {} успешно изменена на {}", recipeId, updatedRatingDTO.getId(), newEstimation);
        return ApiResponse.success(updatedRatingDTO);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalState(IllegalStateException e) {
        log.warn("Обработка исключения IllegalStateException: {} ", e.getMessage());

        Map<String, Object> response = new HashMap<>();
        response.put(FIELD_STATUS, FIELD_ERROR);
        response.put(FIELD_MESSAGE, e.getMessage());
        log.warn("Возврат ответа 409 Conflict: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleEntityNotFound(EntityNotFoundException e) {
        log.warn("Обработка исключения EntityNotFoundException: {} ", e.getMessage());

        Map<String, Object> response = new HashMap<>();
        response.put(FIELD_STATUS, FIELD_ERROR);
        response.put(FIELD_MESSAGE, e.getMessage());
        log.warn("Возврат ответа 404 Not Found: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDenied(AccessDeniedException e) {
        log.warn("Обработка исключения AccessDeniedException: {} ", e.getMessage());

        Map<String, Object> response = new HashMap<>();
        response.put(FIELD_STATUS, FIELD_ERROR);
        response.put(FIELD_MESSAGE, "У вас нет прав для выполнения этого действия.");
        log.warn("Возврат ответа 403 Forbidden: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException e) {
        log.warn("Обработка исключения IllegalArgumentException: {} ", e.getMessage());

        Map<String, Object> response = new HashMap<>();
        response.put(FIELD_STATUS, FIELD_ERROR);
        response.put(FIELD_MESSAGE, e.getMessage());
        log.warn("Возврат ответа 400 Bad Request: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(UnauthorizedUserException.class)
    public ResponseEntity<Map<String, Object>> handleUnauthorizedUser(UnauthorizedUserException e) {
        log.warn("Обработка исключения UnauthorizedUserException: {} ", e.getMessage());

        Map<String, Object> response = new HashMap<>();
        response.put(FIELD_STATUS, FIELD_ERROR);
        response.put(FIELD_MESSAGE, e.getMessage());
        log.warn("Возврат ответа 401 UNAUTHORIZED: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }
}