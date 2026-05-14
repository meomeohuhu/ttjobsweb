package com.ttjobs.backend.dto.forum;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ForumCommentDTO {
    private Long id;
    private Long postId;
    private Long authorId;
    private String author;
    private String authorRole;
    private String body;
    private boolean editable;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

