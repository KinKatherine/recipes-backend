package com.group.collectionofrecipes.controllers;


import com.group.collectionofrecipes.dto.commentdto.CommentDTO;
import com.group.collectionofrecipes.dto.commentdto.CreateCommentDTO;
import com.group.collectionofrecipes.dto.responsedto.ApiResponse;
import com.group.collectionofrecipes.services.CommentService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@Tag(name = "Comments")
@Slf4j
@RestController
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    //update опционально

    //СОЗДАНИЕ КОММЕНТАРИЯ
    @PostMapping("/api/v1/comments")
    public ApiResponse<CommentDTO> createComment(@RequestBody @Valid CreateCommentDTO createCommentDTO,
                                     Principal principal) {

        if (principal == null) {
            log.warn("Попытка создать комментарий без авторизации.");
            return ApiResponse.unSuccess(HttpStatus.UNAUTHORIZED);
        }

        CommentDTO newCommentDTO = commentService.createComment(createCommentDTO, principal);
        if (newCommentDTO == null) {
            log.warn("Не удалось создать комментарий: Пользователь или рецепт не найдены.");
            return ApiResponse.unSuccess(HttpStatus.NOT_FOUND);
        }

        return ApiResponse.success(newCommentDTO);
    }

    // УДАЛЕНИЕ КОММЕНТАРИЯ
    @DeleteMapping("/api/v1/comments/{commentId}")
    public ApiResponse<CommentDTO> deleteComment(@PathVariable Long commentId) {

        try {
            CommentDTO deletedComment = commentService.deleteComment(commentId);
            return ApiResponse.success(deletedComment);

        } catch (EntityNotFoundException e) {
            log.warn("Удаление не удалось: {}", e.getMessage());
            return ApiResponse.unSuccess(HttpStatus.NOT_FOUND);
        }
    }

    // ОБНОВЛЕНИЕ КОММЕНТАРИЯ
    @PutMapping("/api/v1/comments/{commentId}")
    public ApiResponse<CommentDTO> updateComment(@PathVariable Long commentId,
                                                            @RequestParam String newText) {
        try {
            CommentDTO updatedCommentDTO = commentService.updateComment(commentId, newText);
            return ApiResponse.success(updatedCommentDTO);

        } catch (EntityNotFoundException e) {
            log.warn("Обновление комментария ID {} не удалось: {}", commentId, e.getMessage());
            return ApiResponse.unSuccess(HttpStatus.NOT_FOUND);
        }
    }

}
