package com.group.collectionofrecipes.repositories;

import com.group.collectionofrecipes.entities.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment,Long> {
}
