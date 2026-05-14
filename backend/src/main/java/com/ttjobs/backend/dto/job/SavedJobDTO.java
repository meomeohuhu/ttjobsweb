package com.ttjobs.backend.dto.job;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SavedJobDTO {
    private Long id;
    private Long userId;
    private Long jobId;
    private String jobTitle;
    private String companyName;
    private String companyLogoUrl;
    private String jobLocation;
    private java.math.BigDecimal salaryMin;
    private java.math.BigDecimal salaryMax;
    private java.math.BigDecimal salary;
    private String currency;
    private String jobStatus;
    private LocalDateTime savedAt;
    private String note;
    private String tag;
}

