package com.ttjobs.backend.dto;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class ApplicationTimelineDTO {
    private String fromStatus;
    private String toStatus;
    private LocalDateTime changedAt;
}
