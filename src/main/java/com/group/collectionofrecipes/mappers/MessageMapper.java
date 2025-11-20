package com.group.collectionofrecipes.mappers;

import com.group.collectionofrecipes.dto.chatdto.ChatMessageDTO;
import com.group.collectionofrecipes.entities.ChatMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MessageMapper {

    public ChatMessageDTO toChatMessageDTO(ChatMessage msg) {
        return ChatMessageDTO.builder()
                .id(msg.getId())
                .content(msg.getContent())
                .senderEmail(msg.getSender().getEmail())
                .senderUsername(msg.getSender().getUsername())
                .recipientEmail(msg.getRecipient() != null ? msg.getRecipient().getEmail() : null)
                .sentAt(msg.getSentAt())
                .attachmentUrl(msg.getAttachmentUrl())
                .build();
    }
}
