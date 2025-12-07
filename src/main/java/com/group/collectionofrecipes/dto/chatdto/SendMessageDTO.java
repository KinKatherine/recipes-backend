package com.group.collectionofrecipes.dto.chatdto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SendMessageDTO {

    @NotBlank()
    @Size(min = 1, max = 1000)
    private String content;
    private Long recipientId;
    private String attachmentUrl;
}
