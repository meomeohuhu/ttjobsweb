package com.ttjobs.backend.dto.recruiter;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class RecruiterJobDTO {
    private Long id;
    private String title;
    private String description;
    private String location;
    private BigDecimal salary;
    private BigDecimal salaryMin;
    private BigDecimal salaryMax;
    private String currency;
    private String jobType;
    private String experienceLevel;
    private String category;
    private String status;
    private LocalDateTime postedDate;
    private LocalDateTime applicationDeadline;
    private Long companyId;
    private String companyName;
    private String companyLogoUrl;
    private Long applicationCount;
    private Long newApplicationCount;
    private Long savedCount;
}

