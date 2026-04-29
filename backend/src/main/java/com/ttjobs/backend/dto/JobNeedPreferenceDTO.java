package com.ttjobs.backend.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class JobNeedPreferenceDTO {

    private String desiredTitle;
    private String desiredLocation;
    private String desiredCategory;
    private String desiredJobType;
    private String desiredExperienceLevel;
    private BigDecimal minSalary;
    private BigDecimal maxSalary;
    private Boolean remoteOnly;
    private Boolean configured;
    private LocalDateTime updatedAt;
}
