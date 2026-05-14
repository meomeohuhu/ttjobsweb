package com.ttjobs.backend.dto.job;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class JobNeedPreferenceDTO {

    private String desiredTitle;
    private String desiredLocation;
    private String desiredCategory;
    private String desiredJobType;
    private String desiredExperienceLevel;
    private BigDecimal minSalary;
    private BigDecimal maxSalary;
    private List<String> preferredSkills;
    private List<String> excludedKeywords;
    private Boolean remoteOnly;
    private Boolean configured;
    private LocalDateTime updatedAt;
}

