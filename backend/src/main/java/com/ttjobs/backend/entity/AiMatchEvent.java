package com.ttjobs.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "ai_match_events")
@Data
public class AiMatchEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    private Long jobId;

    @Column(nullable = false, length = 80)
    private String eventType;

    @Column(columnDefinition = "TEXT")
    private String cvSnapshotText;

    @Column(columnDefinition = "TEXT")
    private String jobSnapshotText;

    @Column(length = 40)
    private String predictedLabel;

    private Integer predictedScore;

    @Column(length = 80)
    private String source;

    @Column(nullable = false)
    private LocalDateTime createdAt;
}
