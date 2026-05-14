package com.ttjobs.backend.dto.common;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class JobSummaryDTO {
    private Long id;
    private String title;
    private String location;
    private BigDecimal salaryMin;
    private BigDecimal salaryMax;
    private String currency;
    private String jobType;
    private String experienceLevel;
    private String category;
    private String status;
    private LocalDateTime postedDate;
    private CompanySummaryDTO company;
}
