package com.ttjobs.backend.dto.admin;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class AdminAuditLogDTO {
    private Long id;
    private Long actorId;
    private String actorName;
    private String action;
    private String targetType;
    private Long targetId;
    private String reason;
    private String metadata;
    private LocalDateTime createdAt;
}
