package com.group.collectionofrecipes.mappers;

import com.group.collectionofrecipes.dto.commentdto.CommentDTO;
import com.group.collectionofrecipes.dto.commentdto.CreateCommentDTO;
import com.group.collectionofrecipes.entities.Comment;
import com.group.collectionofrecipes.entities.Recipe;
import com.group.collectionofrecipes.entities.User;
import org.springframework.stereotype.Component;

@Component
public class CommentMapper {

    public CommentDTO toCommentDto(Comment comment) {

        return CommentDTO.builder()
                .id(comment.getId())
                .commentText(comment.getCommentText())
                .createdAt(comment.getCreatedAt())
                .authorId(comment.getUser().getId())
                .authorUsername(comment.getUser().getUsername())
                .build();
    }

    public Comment toCommentEntity(CreateCommentDTO commentDTO, User user, Recipe recipe) {

        return Comment.builder()
                .commentText(commentDTO.getCommentText())
                .recipe(recipe)
                .user(user)
                .build();
    }
}
