package com.group.collectionofrecipes.repositories;

import com.group.collectionofrecipes.entities.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    @Query("SELECT m FROM ChatMessage m WHERE m.recipient IS NULL AND m.id > :lastId ORDER BY m.sentAt ASC")
    List<ChatMessage> findPublicMessagesAfterId(@Param("lastId") Long lastId);

    @Query("SELECT m FROM ChatMessage m WHERE m.recipient IS NULL ORDER BY m.sentAt DESC LIMIT 50")
    List<ChatMessage> findLastPublicMessages();

    @Query("SELECT m FROM ChatMessage m " +
           "WHERE (m.sender.id = :userId1 AND m.recipient.id = :userId2) " +
           "   OR (m.sender.id = :userId2 AND m.recipient.id = :userId1) " +
           "ORDER BY m.sentAt ASC")
    List<ChatMessage> findConversation(@Param("userId1") Long userId1, @Param("userId2") Long userId2);

    @Query("SELECT COUNT(m) FROM ChatMessage m WHERE m.recipient.email = :email AND m.isRead = false")
    long countUnreadMessages(@Param("email") String email);
}
