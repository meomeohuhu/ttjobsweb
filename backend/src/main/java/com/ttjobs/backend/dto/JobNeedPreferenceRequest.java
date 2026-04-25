package com.ttjobs.backend.dto;

import jakarta.validation.constraints.DecimalMin;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class JobNeedPreferenceRequest {

    private String desiredTitle;
    private String desiredLocation;
    private String desiredCategory;
    private String desiredJobType;
    private String desiredExperienceLevel;

    @DecimalMin(value = "0.0", inclusive = true)
    private BigDecimal minSalary;

    @DecimalMin(value = "0.0", inclusive = true)
    private BigDecimal maxSalary;

    private Boolean remoteOnly;
}
