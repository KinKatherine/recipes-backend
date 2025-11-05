package com.group.collectionofrecipes.repositories;


import com.group.collectionofrecipes.entities.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    // Получить последние 50 сообщений
    List<ChatMessage> findTop50ByOrderByTimestampDesc();

    // Для очистки старых сообщений
    @Modifying
    @Query("DELETE FROM ChatMessage m WHERE m.timestamp < :date")
    int deleteByTimestampBefore(@Param("date") LocalDateTime date);
}