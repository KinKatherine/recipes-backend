package com.group.collectionofrecipes.dto.chatdto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class ChatMessageDTO {
    private Long id;
    private String content;
    private String senderEmail;
    private String senderUsername;
    private String recipientEmail;
    private LocalDateTime sentAt;
    private String attachmentUrl;
}
