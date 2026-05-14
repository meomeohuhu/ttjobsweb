package com.ttjobs.backend.dto.forum;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ForumReportDTO {
    private Long id;
    private Long postId;
    private Long commentId;
    private Long reporterId;
    private String reporterName;
    private String reason;
    private String details;
    private String status;
    private String moderationAction;
    private String moderationReason;
    private Long resolvedById;
    private String resolvedByName;
    private LocalDateTime createdAt;
    private LocalDateTime resolvedAt;
}

