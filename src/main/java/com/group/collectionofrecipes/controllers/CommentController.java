package com.group.collectionofrecipes.controllers;


import com.group.collectionofrecipes.dto.commentdto.CommentDTO;
import com.group.collectionofrecipes.dto.commentdto.CreateCommentDTO;
import com.group.collectionofrecipes.dto.responsedto.ApiResponse;
import com.group.collectionofrecipes.exceptions.UnauthorizedUserException;
import com.group.collectionofrecipes.services.CommentService;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.group.collectionofrecipes.utils.ApiConstants.FIELD_ERROR;
import static com.group.collectionofrecipes.utils.ApiConstants.FIELD_MESSAGE;
import static com.group.collectionofrecipes.utils.ApiConstants.FIELD_STATUS;

@Tag(name = "Comments")
@Slf4j
@RestController
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    //СОЗДАНИЕ КОММЕНТАРИЯ
    @PostMapping("/api/v1/comments")
    public ApiResponse<CommentDTO> createComment(@RequestBody @Valid CreateCommentDTO createCommentDTO,
                                                 Principal principal) {

        log.info("POST /api/v1/comments");
        CommentDTO newCommentDTO = commentService.createComment(createCommentDTO, principal);
        log.info("POST /api/v1/comments - комментарий для рецепта с id {} успешно добавлен", createCommentDTO.getRecipeId());
        return ApiResponse.success(newCommentDTO);
    }

    //УДАЛЕНИЕ КОММЕНТАРИЯ
    @DeleteMapping("/api/v1/comments/{commentId}")
    public ApiResponse<CommentDTO> deleteComment(@PathVariable Long commentId) {

        log.info("DELETE /api/v1/comments/{}", commentId);
        CommentDTO deletedComment = commentService.deleteComment(commentId);
        log.info("DELETE /api/v1/comments/{} - комментарий с id {} успешно удален", commentId, commentId);
        return ApiResponse.success(deletedComment);
    }

    //ОБНОВЛЕНИЕ КОММЕНТАРИЯ
    //не надо
    @PutMapping("/api/v1/comments/{commentId}")
    public ApiResponse<CommentDTO> updateComment(@PathVariable Long commentId,
                                                 @RequestParam String newText,
                                                 Principal principal) {
        log.info("PUT /api/v1/comments/{}", commentId);
        CommentDTO updatedCommentDTO = commentService.updateComment(commentId, newText, principal);
        log.info("PUT /api/v1/comments/{} - текст комментария с id {} изменен на {}", commentId, commentId, newText);
        return ApiResponse.success(updatedCommentDTO);
    }


    @GetMapping("/api/v1/comments")
    public ApiResponse<List<CommentDTO>> getUserComments(Principal principal) {
        log.info("GET /api/v1/comments");
        List<CommentDTO> commentDTOList = commentService.getUserComments(principal);
        log.info("GET /api/v1/comments для пользователя {}", principal.getName());
        return ApiResponse.success(commentDTOList);
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

    @ExceptionHandler(UnauthorizedUserException.class)
    public ResponseEntity<Map<String, Object>> handleUnauthorizedUser(UnauthorizedUserException e) {
        log.warn("Обработка исключения UnauthorizedUserException: {} ", e.getMessage());

        Map<String, Object> response = new HashMap<>();
        response.put(FIELD_STATUS, FIELD_ERROR);
        response.put(FIELD_MESSAGE, "У вас нет прав для выполнения этого действия.");
        log.warn("Возврат ответа 401 UNAUTHORIZED: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }
}