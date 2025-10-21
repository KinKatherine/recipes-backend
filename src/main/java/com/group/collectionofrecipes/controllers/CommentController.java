package com.group.collectionofrecipes.controllers;


import com.group.collectionofrecipes.dto.commentdto.CommentDTO;
import com.group.collectionofrecipes.dto.commentdto.CreateCommentDTO;
import com.group.collectionofrecipes.services.CommentService;
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

import static com.group.collectionofrecipes.utils.ApiConstants.FIELD_COMMENT;
import static com.group.collectionofrecipes.utils.ApiConstants.FIELD_MESSAGE;
import static com.group.collectionofrecipes.utils.ApiConstants.FIELD_STATUS;
import static com.group.collectionofrecipes.utils.ApiConstants.FIELD_SUCCESS;

@Tag(name = "Comments")
@Slf4j
@RestController
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    //update опционально

    //СОЗДАНИЕ КОММЕНТАРИЯ
    @PostMapping("/api/v1/comments")
    public ResponseEntity<Map<String,Object>> createComment(@RequestBody @Valid CreateCommentDTO createCommentDTO,
                                                            Principal principal) {

        if (principal == null) {
            log.warn("Попытка создать комментарий без авторизации.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        CommentDTO newCommentDTO = commentService.createComment(createCommentDTO, principal);
        if (newCommentDTO == null) {
            log.warn("Не удалось создать комментарий: Пользователь или рецепт не найдены.");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        Map<String,Object> response = new HashMap<>();
        response.put(FIELD_STATUS, FIELD_SUCCESS);
        response.put(FIELD_COMMENT, newCommentDTO);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // УДАЛЕНИЕ КОММЕНТАРИЯ
    @DeleteMapping("/api/v1/comments/{commentId}")
    public ResponseEntity<Map<String,Object>> deleteComment(@PathVariable Long commentId) {

        try {
            Long deletedCommentId = commentService.deleteComment(commentId);

            Map<String,Object> response = new HashMap<>();
            response.put(FIELD_STATUS, FIELD_SUCCESS);
            response.put("deletedCommentId", deletedCommentId);
            return ResponseEntity.ok(response);

        } catch (EntityNotFoundException e) {
            log.warn("Удаление не удалось: {}", e.getMessage());
            Map<String,Object> errorResponse = new HashMap<>();
            errorResponse.put(FIELD_STATUS, "error");
            errorResponse.put(FIELD_MESSAGE, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }

    // ОБНОВЛЕНИЕ КОММЕНТАРИЯ
    @PutMapping("/api/v1/comments/{commentId}")
    public ResponseEntity<Map<String,Object>> updateComment(@PathVariable Long commentId,
                                                            @RequestParam String newText) {
        try {
            CommentDTO updatedCommentDTO = commentService.updateComment(commentId, newText);

            Map<String, Object> response = new HashMap<>();
            response.put(FIELD_STATUS, FIELD_SUCCESS);
            response.put(FIELD_COMMENT, updatedCommentDTO);
            return ResponseEntity.ok(response);

        } catch (EntityNotFoundException e) {
            log.warn("Обновление комментария ID {} не удалось: {}", commentId, e.getMessage());
            Map<String,Object> errorResponse = new HashMap<>();
            errorResponse.put(FIELD_STATUS, "error");
            errorResponse.put(FIELD_MESSAGE, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }

}
