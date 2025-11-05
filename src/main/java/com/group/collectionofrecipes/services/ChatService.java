package com.group.collectionofrecipes.services;


import com.group.collectionofrecipes.entities.ChatMessage;
import com.group.collectionofrecipes.repositories.ChatMessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatService {

    private final ChatMessageRepository messageRepository;

    public ChatMessage saveMessage(ChatMessage message) {
        if (message.getTimestamp() == null) {
            message.setTimestamp(LocalDateTime.now());
        }
        return messageRepository.save(message);
    }

    public List<ChatMessage> getMessageHistory() {
        return messageRepository.findTop50ByOrderByTimestampDesc();
    }

    public List<ChatMessage> getRecentMessages() {
        return messageRepository.findTop50ByOrderByTimestampDesc(); // Пока просто 50 последних
    }

    // Очистка сообщений старше 30 дней (каждый день в 2 ночи)
    @Scheduled(cron = "0 0 2 * * ?")
    @Transactional
    public void cleanupOldMessages() {
        LocalDateTime monthAgo = LocalDateTime.now().minusDays(30);
        int deletedCount = messageRepository.deleteByTimestampBefore(monthAgo);
        if (deletedCount > 0) {
            log.info("Очистка чата: удалено {} сообщений старше {}", deletedCount, monthAgo);
        }
    }
}
