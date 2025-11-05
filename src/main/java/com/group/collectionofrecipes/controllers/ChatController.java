package com.group.collectionofrecipes.controllers;

import com.group.collectionofrecipes.entities.ChatMessage;
import com.group.collectionofrecipes.services.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

@Controller
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;
    private final AtomicLong guestCounter = new AtomicLong(1);

    @MessageMapping("/chat.sendMessage")
    @SendTo("/topic/public")
    public ChatMessage sendMessage(@Payload ChatMessage chatMessage,
                                   Principal principal,
                                   SimpMessageHeaderAccessor headerAccessor) {

        String username = resolveUsername(principal, headerAccessor);
        chatMessage.setSender(username);
        chatMessage.setTimestamp(LocalDateTime.now());

        chatService.saveMessage(chatMessage);

        return chatMessage;
    }

    @MessageMapping("/chat.addUser")
    @SendTo("/topic/public")
    public ChatMessage addUser(@Payload ChatMessage chatMessage,
                               Principal principal,
                               SimpMessageHeaderAccessor headerAccessor) {

        String username = resolveUsername(principal, headerAccessor);

        Optional.ofNullable(headerAccessor)
                .map(SimpMessageHeaderAccessor::getSessionAttributes)
                .ifPresent(attrs -> attrs.put("username", username));

        chatMessage.setSender(username);
        chatMessage.setTimestamp(LocalDateTime.now());

        chatService.saveMessage(chatMessage);

        return chatMessage;
    }

    private String resolveUsername(Principal principal, SimpMessageHeaderAccessor headerAccessor) {
        String sessionUsername = Optional.ofNullable(headerAccessor)
                .map(SimpMessageHeaderAccessor::getSessionAttributes)
                .map(attrs -> (String) attrs.get("username"))
                .orElse(null);

        if (sessionUsername != null) {
            return sessionUsername;
        }

        String principalName = Optional.ofNullable(principal)
                .map(Principal::getName)
                .filter(name -> !"anonymousUser".equals(name))
                .orElse(null);

        if (principalName != null) {
            return principalName;
        }

        return "Гость" + guestCounter.getAndIncrement();
    }
}