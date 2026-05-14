package com.ttjobs.backend.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class JobNeedPreference {

    private Long userId;

    private String desiredTitle;
    private String desiredLocation;
    private String desiredCategory;
    private String desiredJobType;
    private String desiredExperienceLevel;

    private BigDecimal minSalary;

    private BigDecimal maxSalary;

    private String preferredSkills;

    private String excludedKeywords;

    private Boolean remoteOnly;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
