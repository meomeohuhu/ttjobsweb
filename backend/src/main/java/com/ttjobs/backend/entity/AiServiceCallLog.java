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
@Table(name = "ai_service_call_logs")
@Data
public class AiServiceCallLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 160)
    private String endpoint;

    @Column(nullable = false, length = 40)
    private String status;

    private Integer httpStatus;

    @Column(nullable = false)
    private Long latencyMs;

    @Column(nullable = false)
    private Boolean fallbackUsed;

    @Column(length = 40)
    private String predictedLabel;

    private Double confidence;

    @Column(nullable = false)
    private LocalDateTime createdAt;
}
