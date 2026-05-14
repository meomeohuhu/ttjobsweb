package com.ttjobs.backend.dto.admin;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AiMatchEventDTO {
    private Long id;
    private Long userId;
    private Long jobId;
    private String eventType;
    private String cvSnapshotText;
    private String jobSnapshotText;
    private String predictedLabel;
    private Integer predictedScore;
    private String source;
    private LocalDateTime createdAt;
}
