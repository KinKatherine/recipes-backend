package com.group.collectionofrecipes.dto.chatdto;

import lombok.Data;

@Data
public class SendMessageDTO {
    private String content;
    private Long recipientId;
    private String attachmentUrl;
}
