package com.ttjobs.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "application_ai_scores")
@Data
public class ApplicationAiScore {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "application_id", nullable = false)
    private JobApplication application;

    @Column(nullable = false)
    private Integer score;

    @Column(nullable = false)
    private String level;

    private Double rawScore;

    @Column(columnDefinition = "TEXT")
    private String signals;

    @Column(columnDefinition = "TEXT")
    private String pros;

    @Column(columnDefinition = "TEXT")
    private String cons;

    @Column(nullable = false)
    private String cvHash;

    @Column(nullable = false)
    private String jobHash;

    @Column(nullable = false)
    private LocalDateTime scoredAt;
}
