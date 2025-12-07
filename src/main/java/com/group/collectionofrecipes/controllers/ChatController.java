package com.group.collectionofrecipes.controllers;

import com.group.collectionofrecipes.dto.chatdto.ChatMessageDTO;
import com.group.collectionofrecipes.dto.chatdto.SendMessageDTO;
import com.group.collectionofrecipes.dto.responsedto.ApiResponse;
import com.group.collectionofrecipes.services.ChatService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@Tag(name = "Chat")
@Slf4j
@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @PostMapping
    public ApiResponse<ChatMessageDTO> sendMessage(@RequestBody @Valid SendMessageDTO messageDTO,
                                                    Principal principal) {
        log.info("POST  /chat");
        ChatMessageDTO chatMessageDTO =  chatService.sendMessage(principal.getName(), messageDTO);
        log.info("POST  /chat - сообщение с id {} отправлено", chatMessageDTO.getId());
        return ApiResponse.success(chatMessageDTO);
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, String>> uploadAttachment( @RequestParam("file") MultipartFile file) {
        String fileUrl = chatService.uploadFile(file);
        return ResponseEntity.ok(Map.of("url", fileUrl));
    }

    @GetMapping("/public")
    public ResponseEntity<List<ChatMessageDTO>> getPublicHistory(
            @RequestParam(value = "from_id", defaultValue = "0") Long fromId
    ) {
        return ResponseEntity.ok(chatService.getPublicHistory(fromId));
    }

    @GetMapping("/private/{userId}")
    public ResponseEntity<List<ChatMessageDTO>> getPrivateHistory(
            @PathVariable Long userId,
            Principal principal
    ) {
        return ResponseEntity.ok(chatService.getPrivateHistory(principal.getName(), userId));
    }
}
