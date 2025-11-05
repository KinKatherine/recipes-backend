package com.group.collectionofrecipes.config;

import com.group.collectionofrecipes.entities.ChatMessage;
import com.group.collectionofrecipes.enums.MessageType;
import com.group.collectionofrecipes.services.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketEventListener {

    private final SimpMessageSendingOperations messageTemplate;
    private final ChatService chatService;

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String username = (String) headerAccessor.getSessionAttributes().get("username");

        if (username != null) {
            log.info("Пользователь {} вышел", username);

            ChatMessage chatMessage = ChatMessage.builder()
                    .content(username + " вышел из чата")
                    .type(MessageType.LEAVE)
                    .sender(username)
                    .timestamp(LocalDateTime.now())
                    .build();

            chatService.saveMessage(chatMessage);

            messageTemplate.convertAndSend("/topic/public", chatMessage);
        }
    }
}