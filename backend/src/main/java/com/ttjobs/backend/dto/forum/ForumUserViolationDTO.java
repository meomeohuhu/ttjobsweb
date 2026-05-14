package com.ttjobs.backend.dto.forum;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class ForumUserViolationDTO {
    private Long id;
    private Long userId;
    private String userName;
    private Integer warningCount;
    private LocalDateTime mutedUntil;
    private String lastReason;
    private LocalDateTime lastActionAt;
}
