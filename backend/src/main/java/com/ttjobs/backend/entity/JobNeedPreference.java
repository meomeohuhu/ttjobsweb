package com.ttjobs.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "job_need_preferences")
@Data
public class JobNeedPreference {

    @Id
    private Long userId;

    private String desiredTitle;
    private String desiredLocation;
    private String desiredCategory;
    private String desiredJobType;
    private String desiredExperienceLevel;

    @Column(precision = 19, scale = 2)
    private BigDecimal minSalary;

    @Column(precision = 19, scale = 2)
    private BigDecimal maxSalary;

    @Column(nullable = false)
    private Boolean remoteOnly;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    public void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) {
            createdAt = now;
        }
        if (updatedAt == null) {
            updatedAt = now;
        }
        if (remoteOnly == null) {
            remoteOnly = false;
        }
    }

    @PreUpdate
    public void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
