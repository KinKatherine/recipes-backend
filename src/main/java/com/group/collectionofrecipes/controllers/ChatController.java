package com.group.collectionofrecipes.controllers;

import com.group.collectionofrecipes.dto.ChatMessage;
import com.group.collectionofrecipes.enums.MessageType;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import static com.group.collectionofrecipes.utils.ApiConstants.USERNAME;

@Tag(name = "Chat")
@Controller
public class ChatController {

    private final Map<String, String> guestToUserMap = new ConcurrentHashMap<>();
    private final AtomicLong guestCounter = new AtomicLong(1);

    @MessageMapping("/chat.sendMessage")
    @SendTo("/topic/public")
    public ChatMessage sendMessage(@Payload ChatMessage chatMessage,
                                   Principal principal,
                                   SimpMessageHeaderAccessor headerAccessor) {

        String username = resolveUsername(principal, headerAccessor);
        chatMessage.setSender(username);
        chatMessage.setTimestamp(LocalDateTime.now().toString());

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
                .ifPresent(attrs -> attrs.put(USERNAME, username));

        chatMessage.setSender(username);
        return chatMessage;
    }

    @MessageMapping("/chat.userLoggedIn")
    @SendTo("/topic/public")
    public ChatMessage userLoggedIn(Principal principal,
                                    SimpMessageHeaderAccessor headerAccessor) {

        if (principal == null) {
            return createSystemMessage("Ошибка: пользователь не авторизован");
        }

        String newUsername = principal.getName();
        String oldUsername = Optional.ofNullable(headerAccessor)
                .map(SimpMessageHeaderAccessor::getSessionAttributes)
                .map(attrs -> (String) attrs.get(USERNAME))
                .orElse(null);

        Optional.ofNullable(headerAccessor)
                .map(SimpMessageHeaderAccessor::getSessionAttributes)
                .ifPresent(attrs -> attrs.put(USERNAME, newUsername));


        if (oldUsername != null && oldUsername.startsWith("Гость")) {
            guestToUserMap.put(oldUsername, newUsername);
        }

        String messageContent = (oldUsername != null)
                ? oldUsername + " теперь известен как " + newUsername
                : "Пользователь " + newUsername + " присоединился к чату";

        return createSystemMessage(messageContent);
    }

    private ChatMessage createSystemMessage(String content) {
        return ChatMessage.builder()
                .type(MessageType.CHAT)
                .sender("Система")
                .content(content)
                .timestamp(LocalDateTime.now().toString())
                .build();
    }

    private String resolveUsername(Principal principal, SimpMessageHeaderAccessor headerAccessor) {
        if (principal != null && !"anonymousUser".equals(principal.getName())) {
            return principal.getName();
        }

        String sessionUser = Optional.ofNullable(headerAccessor)
                .map(SimpMessageHeaderAccessor::getSessionAttributes)
                .map(attrs -> (String) attrs.get(USERNAME))
                .orElse(null);

        if (sessionUser != null) {
            return sessionUser;
        }

        return "Гость" + guestCounter.getAndIncrement();
    }
}