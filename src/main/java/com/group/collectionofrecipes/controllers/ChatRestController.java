package com.group.collectionofrecipes.controllers;

import com.group.collectionofrecipes.entities.ChatMessage;
import com.group.collectionofrecipes.services.ChatService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "СhatMessages")
@RestController
@RequiredArgsConstructor
public class ChatRestController {

    private final ChatService chatService;

    @GetMapping("/api/chat/messages")
    public ResponseEntity<List<ChatMessage>> getMessageHistory() {
        return ResponseEntity.ok(chatService.getMessageHistory());
    }
}
