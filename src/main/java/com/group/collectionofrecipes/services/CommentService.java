package com.group.collectionofrecipes.services;

import com.group.collectionofrecipes.dto.commentdto.CommentDTO;
import com.group.collectionofrecipes.dto.commentdto.CreateCommentDTO;
import com.group.collectionofrecipes.entities.Comment;
import com.group.collectionofrecipes.entities.Recipe;
import com.group.collectionofrecipes.entities.User;
import com.group.collectionofrecipes.exceptions.UnauthorizedUserException;
import com.group.collectionofrecipes.mappers.CommentMapper;
import com.group.collectionofrecipes.repositories.CommentRepository;
import com.group.collectionofrecipes.repositories.RecipeRepository;
import com.group.collectionofrecipes.repositories.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

import static com.group.collectionofrecipes.utils.ApiConstants.ERROR_COMMENT_NOT_FOUND;
import static com.group.collectionofrecipes.utils.ApiConstants.ERROR_USER_NOT_FOUND;
import static com.group.collectionofrecipes.utils.ApiConstants.UNAUTHORIZED_USER;

@Service
@Slf4j
@RequiredArgsConstructor
public class CommentService {
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final RecipeRepository recipeRepository;
    private final CommentMapper commentMapper;


    @Transactional
    public CommentDTO createComment(CreateCommentDTO createCommentDTO, Principal principal) {

        if (principal == null) {
            log.warn("Попытка создать комментарий без авторизации.");
            throw new UnauthorizedUserException(UNAUTHORIZED_USER);
        }

        log.info("Запрос на создание комментария для рецепта с id {}", createCommentDTO.getRecipeId());
        String username = principal.getName();
        Long recipeId = createCommentDTO.getRecipeId();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.warn("Пользователь {} не найден при попытке создания комментария.", username);
                    return new EntityNotFoundException(ERROR_USER_NOT_FOUND + username);
                });

        Recipe recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> {
                    log.warn("Рецепт ID {} не найден при попытке создания комментария.", recipeId);
                    return new EntityNotFoundException("Рецепт не найден по id: " + recipeId);
                });

        Comment newComment = commentMapper.toCommentEntity(createCommentDTO, user, recipe);
        Comment savedComment = commentRepository.save(newComment);

        log.info("Комментарий ID {} успешно создан пользователем {} для рецепта ID {}.",
                savedComment.getId(), username, recipeId);

        return commentMapper.toCommentDto(savedComment);
    }

    @Transactional
    public CommentDTO deleteComment(Long commentId) {

        log.info("Запрос  админа на удаление комментария с id {}", commentId);
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> {
                    log.warn("Комментарий ID {} не найден для удаления.", commentId);
                    return new EntityNotFoundException(ERROR_COMMENT_NOT_FOUND + commentId);
                });

        Long recipeId = comment.getRecipe().getId();
        commentRepository.delete(comment);
        log.info("Комментарий ID {} успешно удален для рецепта ID {}.", comment.getId(), recipeId);

        return commentMapper.toCommentDto(comment);
    }

    @Transactional
    public CommentDTO updateComment(Long commentId, String newText, Principal principal) {

        if (principal == null) {
            log.warn("Попытка обновить комментарий без авторизации.");
            throw new UnauthorizedUserException(UNAUTHORIZED_USER);
        }

        log.info("Запрос пользователя {} на обновление комментария с id {}", principal.getName(), commentId);
        Comment commentToUpdate = commentRepository.findById(commentId)
                .orElseThrow(() -> {
                    log.warn("Комментарий ID {} не найден для обновления.", commentId);
                    return new EntityNotFoundException(ERROR_COMMENT_NOT_FOUND + commentId);
                });

        String currentUsername = principal.getName();
        if (!commentToUpdate.getUser().getUsername().equals(currentUsername)) {
            log.warn("Пользователь {} попытался обновить комментарий ID {}, принадлежащий {}. Доступ запрещен.",
                    currentUsername, commentId, commentToUpdate.getUser().getUsername());
            throw new AccessDeniedException("Вы не являетесь автором этого комментария и не можете его изменить.");
        }

        String oldText = commentToUpdate.getCommentText();
        commentToUpdate.setCommentText(newText);
        Comment updatedComment = commentRepository.save(commentToUpdate);

        log.info("Комментарий ID {} успешно обновлен пользователем {}. Текст: '{}' -> '{}'.",
                commentId, currentUsername, oldText, newText);

        return commentMapper.toCommentDto(updatedComment);
    }

    @Transactional(readOnly = true)
    public List<CommentDTO> getUserComments(Principal principal) {
        if (principal == null) {
            log.error(UNAUTHORIZED_USER);
            throw new UnauthorizedUserException(UNAUTHORIZED_USER);
        }
        String username = principal.getName();
        User user = userRepository.findByUsername(username) .orElseThrow(() -> {
            log.warn("Пользователь {} не найден", username);
            return new EntityNotFoundException(ERROR_USER_NOT_FOUND + username);
        });
        List<Comment> comments = commentRepository.findCommentsByUserId(user.getId());
        List<CommentDTO> commentDTOS = new ArrayList<>();
        for (Comment comment : comments) {
            commentDTOS.add(commentMapper.toCommentDto(comment));
        }
        return commentDTOS;
    }
}
