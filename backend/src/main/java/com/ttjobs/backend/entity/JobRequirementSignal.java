package com.ttjobs.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

@Entity
@Table(name = "job_requirement_signals")
@Data
public class JobRequirementSignal {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id")
    private Job job;
    private String normalizedTitle;
    private String seniority;
    @Column(columnDefinition = "TEXT")
    private String skills;
    @Column(columnDefinition = "TEXT")
    private String industries;
    @Column(columnDefinition = "TEXT")
    private String locations;
    private BigDecimal salaryMin;
    private BigDecimal salaryMax;
    private String currency;
    @Column(columnDefinition = "TEXT")
    private String languages;
    @Column(columnDefinition = "TEXT")
    private String evidence;
    @Column(columnDefinition = "TEXT")
    private String rawText;
    private String source;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    @PrePersist public void onCreate() { LocalDateTime now = LocalDateTime.now(); createdAt = now; updatedAt = now; }
    @PreUpdate public void onUpdate() { updatedAt = LocalDateTime.now(); }
}
