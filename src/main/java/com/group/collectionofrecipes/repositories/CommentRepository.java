package com.group.collectionofrecipes.repositories;

import com.group.collectionofrecipes.entities.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    List<Comment> findCommentsByUserId(Long id);
}
