package com.group.collectionofrecipes.dto.commentdto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CommentDTO {

    private Long id;
    private String commentText;
    private LocalDateTime createdAt;
    private Long authorId;
    private String authorUsername;
}
