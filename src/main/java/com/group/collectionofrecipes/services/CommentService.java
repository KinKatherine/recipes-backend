package com.group.collectionofrecipes.services;

import com.group.collectionofrecipes.dto.commentdto.CommentDTO;
import com.group.collectionofrecipes.dto.commentdto.CreateCommentDTO;
import com.group.collectionofrecipes.entities.Comment;
import com.group.collectionofrecipes.entities.Recipe;
import com.group.collectionofrecipes.entities.User;
import com.group.collectionofrecipes.mappers.CommentMapper;
import com.group.collectionofrecipes.repositories.CommentRepository;
import com.group.collectionofrecipes.repositories.RecipeRepository;
import com.group.collectionofrecipes.repositories.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;

import static com.group.collectionofrecipes.utils.ApiConstants.ERROR_COMMENT_NOT_FOUND;
import static com.group.collectionofrecipes.utils.ApiConstants.ERROR_USER_NOT_FOUND;

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

        String username = principal.getName();
        Long recipeId = createCommentDTO.getRecipeId();

        try {
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new EntityNotFoundException(ERROR_USER_NOT_FOUND + username));

            Recipe recipe = recipeRepository.findById(recipeId)
                    .orElseThrow(() -> new EntityNotFoundException("Рецепт не найден по id: " + recipeId));

            Comment newComment = commentMapper.toCommentEntity(createCommentDTO, user, recipe);
            Comment savedComment = commentRepository.save(newComment);
            log.info("Комментарий ID {} успешно создан пользователем {} для рецепта ID {}.", savedComment.getId(), username, recipeId);

            return commentMapper.toCommentDto(savedComment);

        } catch (EntityNotFoundException e) {
            log.error("Не удалось создать комментарий для рецепта ID {}. Ошибка: {}", recipeId, e.getMessage(), e);
            return null;
        }
    }

    @Transactional
    public Long deleteComment(Long commentId) {

        try {
            Comment comment = commentRepository.findById(commentId).orElseThrow(() -> new EntityNotFoundException(ERROR_COMMENT_NOT_FOUND + commentId));
            commentRepository.delete(comment);
            log.info("Комментарий ID {} успешно удален админисиратором (или по ID) для рецепта ID {}.", comment.getId(), comment.getRecipe().getId());
            return commentId;

        } catch (EntityNotFoundException e) {
            log.error("Не удалось удалить комментарий с id {}. Ошибка: {}", commentId,e.getMessage(), e);
            throw e;
        }
    }

    @Transactional
    public CommentDTO updateComment(Long commentId, String newText) {
        try {
            Comment commentToUpdate = commentRepository.findById(commentId)
                    .orElseThrow(() -> {
                        log.error("Комментарий ID {} не найден для обновления.", commentId);
                        return new EntityNotFoundException(ERROR_COMMENT_NOT_FOUND + commentId);
                    });

            String oldText = commentToUpdate.getCommentText();
            commentToUpdate.setCommentText(newText);
            Comment updatedComment = commentRepository.save(commentToUpdate);

            log.info("Комментарий ID {} обновлен: текст изменен с '{}' на '{}'.", commentId, oldText, newText);

            return commentMapper.toCommentDto(updatedComment);

        } catch (EntityNotFoundException e) {
            log.error("Не удалось обновить комментарий с id {}. Ошибка: {}", commentId, e.getMessage(), e);
            throw e;
        }
    }
}
