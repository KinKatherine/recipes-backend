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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.Optional;

import static com.group.collectionofrecipes.utils.ApiConstants.ERROR_COMMENT_NOT_FOUND;
import static com.group.collectionofrecipes.utils.ApiConstants.UNAUTHORIZED_USER;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock
    private CommentRepository commentRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private RecipeRepository recipeRepository;
    @Mock
    private CommentMapper commentMapper;

    @Mock
    private Principal principal;

    @InjectMocks
    private CommentService commentService;

    private final String username = "testUser";
    private final Long userId = 1L;
    private final Long recipeId = 10L;
    private final Long commentId = 100L;
    private final String initialText = "Это отличный рецепт!";
    private final String newText = "Я обновил свой комментарий.";
    private final String anotherUsername = "otherUser";
    private final LocalDateTime now = LocalDateTime.now();

    private User user;
    private Recipe recipe;
    private Comment comment;
    private CommentDTO commentDTO;
    private CreateCommentDTO createCommentDTO;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(userId);
        user.setUsername(username);

        recipe = new Recipe();
        recipe.setId(recipeId);

        comment = new Comment();
        comment.setId(commentId);
        comment.setCommentText(initialText);
        comment.setUser(user);
        comment.setRecipe(recipe);

        comment.setCreatedAt(now);


        commentDTO = CommentDTO.builder()
                .id(commentId)
                .commentText(initialText)
                .createdAt(now)
                .authorId(userId)
                .authorUsername(username)
                .build();


        createCommentDTO = new CreateCommentDTO();
        createCommentDTO.setRecipeId(recipeId);
        createCommentDTO.setCommentText(initialText);

        Mockito.lenient().when(principal.getName()).thenReturn(username);
    }


    @Test
    @DisplayName("Успешное создание комментария")
    void createComment_Success() {
        // Arrange
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(recipeRepository.findById(recipeId)).thenReturn(Optional.of(recipe));
        when(commentMapper.toCommentEntity(any(CreateCommentDTO.class), any(User.class), any(Recipe.class))).thenReturn(comment);
        when(commentRepository.save(any(Comment.class))).thenReturn(comment);
        when(commentMapper.toCommentDto(any(Comment.class))).thenReturn(commentDTO);

        // Act
        CommentDTO result = commentService.createComment(createCommentDTO, principal);

        // Assert
        assertNotNull(result);
        assertEquals(commentId, result.getId());
        assertEquals(initialText, result.getCommentText());
        assertEquals(userId, result.getAuthorId(), "Должен быть userId как authorId.");
        assertEquals(username, result.getAuthorUsername(), "Должен быть username как authorUsername.");
        assertNotNull(result.getCreatedAt());

        // Verify
        verify(userRepository).findByUsername(username);
        verify(recipeRepository).findById(recipeId);
        verify(commentRepository).save(any(Comment.class));
    }

    @Test
    @DisplayName("createComment: Ошибка - Principal == null")
    void createComment_PrincipalIsNull_ThrowsUnauthorizedUserException() {
        // Act & Assert
        UnauthorizedUserException exception = assertThrows(UnauthorizedUserException.class,
                () -> commentService.createComment(createCommentDTO, null));
        assertEquals(UNAUTHORIZED_USER, exception.getMessage());
        verifyNoInteractions(userRepository, recipeRepository, commentRepository, commentMapper);
    }

    @Test
    @DisplayName("createComment: Ошибка - Пользователь не найден")
    void createComment_UserNotFound_ThrowsEntityNotFoundException() {
        // Arrange
        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class,
                () -> commentService.createComment(createCommentDTO, principal));

        // Verify
        verify(userRepository).findByUsername(username);
        verifyNoMoreInteractions(recipeRepository, commentRepository, commentMapper);
    }

    @Test
    @DisplayName("createComment: Ошибка - Рецепт не найден")
    void createComment_RecipeNotFound_ThrowsEntityNotFoundException() {
        // Arrange
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(recipeRepository.findById(recipeId)).thenReturn(Optional.empty());

        // Act & Assert
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> commentService.createComment(createCommentDTO, principal));

        assertEquals("Рецепт не найден по id: " + recipeId, exception.getMessage());

        // Verify
        verify(userRepository).findByUsername(username);
        verify(recipeRepository).findById(recipeId);
        verifyNoMoreInteractions(commentRepository, commentMapper);
    }


    @Test
    @DisplayName("Успешное удаление комментария админом")
    void deleteComment_Success() {
        // Arrange
        when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));
        when(commentMapper.toCommentDto(any(Comment.class))).thenReturn(commentDTO);

        // Act
        CommentDTO result = commentService.deleteComment(commentId);

        // Assert
        assertNotNull(result);
        assertEquals(commentId, result.getId());

        // Verify
        verify(commentRepository).findById(commentId);
        verify(commentRepository).delete(comment);
        verify(commentMapper).toCommentDto(comment);
    }

    @Test
    @DisplayName("deleteComment: Ошибка - Комментарий не найден")
    void deleteComment_CommentNotFound_ThrowsEntityNotFoundException() {
        // Arrange
        when(commentRepository.findById(commentId)).thenReturn(Optional.empty());

        // Act & Assert
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> commentService.deleteComment(commentId));

        assertEquals(ERROR_COMMENT_NOT_FOUND + commentId, exception.getMessage());

        // Verify
        verify(commentRepository).findById(commentId);
        verify(commentRepository, never()).delete(any());
        verifyNoInteractions(commentMapper);
    }

    @Test
    @DisplayName("Успешное обновление комментария автором")
    void updateComment_Success() {
        // Arrange
        Comment updatedCommentEntity = new Comment();
        updatedCommentEntity.setId(commentId);
        updatedCommentEntity.setCommentText(newText);
        updatedCommentEntity.setUser(user);
        updatedCommentEntity.setRecipe(recipe);
        updatedCommentEntity.setCreatedAt(now);

        CommentDTO updatedCommentDTO = CommentDTO.builder()
                .id(commentId)
                .commentText(newText)
                .createdAt(now)
                .authorId(userId)
                .authorUsername(username)
                .build();

        when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));
        when(commentRepository.save(any(Comment.class))).thenReturn(updatedCommentEntity);
        when(commentMapper.toCommentDto(any(Comment.class))).thenReturn(updatedCommentDTO);

        // Act
        CommentDTO result = commentService.updateComment(commentId, newText, principal);

        // Assert
        assertNotNull(result);
        assertEquals(newText, result.getCommentText());

        assertEquals(newText, comment.getCommentText(), "Текст комментария в сущности должен быть обновлен.");

        // Verify
        verify(commentRepository).findById(commentId);
        verify(commentRepository).save(comment);
        verify(commentMapper).toCommentDto(updatedCommentEntity);
    }

    @Test
    @DisplayName("updateComment: Ошибка - Principal == null")
    void updateComment_PrincipalIsNull_ThrowsUnauthorizedUserException() {
        // Act & Assert
        UnauthorizedUserException exception = assertThrows(UnauthorizedUserException.class,
                () -> commentService.updateComment(commentId, newText, null));
        assertEquals(UNAUTHORIZED_USER, exception.getMessage());
        verifyNoInteractions(commentRepository, commentMapper);
    }

    @Test
    @DisplayName("updateComment: Ошибка - Комментарий не найден")
    void updateComment_CommentNotFound_ThrowsEntityNotFoundException() {
        // Arrange
        when(commentRepository.findById(commentId)).thenReturn(Optional.empty());

        // Act & Assert
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> commentService.updateComment(commentId, newText, principal));

        assertEquals(ERROR_COMMENT_NOT_FOUND + commentId, exception.getMessage());

        // Verify
        verify(commentRepository).findById(commentId);
        verify(commentRepository, never()).save(any());
        verifyNoInteractions(commentMapper);
    }

    @Test
    @DisplayName("updateComment: Ошибка - Доступ запрещен (другой пользователь)")
    void updateComment_AccessDenied_ThrowsAccessDeniedException() {
        // Arrange
        when(principal.getName()).thenReturn(anotherUsername);
        when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));

        // Act & Assert
        AccessDeniedException exception = assertThrows(AccessDeniedException.class,
                () -> commentService.updateComment(commentId, newText, principal));

        assertEquals("Вы не являетесь автором этого комментария и не можете его изменить.", exception.getMessage());

        // Verify
        verify(commentRepository).findById(commentId);
        verify(commentRepository, never()).save(any());
        verifyNoInteractions(commentMapper);
    }
}