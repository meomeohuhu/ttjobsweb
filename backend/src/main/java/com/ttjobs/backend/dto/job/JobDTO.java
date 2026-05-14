package com.ttjobs.backend.dto.job;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class JobDTO {
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
    private String imageUrl;
    private String status;
    private LocalDateTime postedDate;
    private LocalDateTime applicationDeadline;
    private Long companyId;
    private String companyName;
    private String companyLogoUrl;
    private Long savedCount;
    private Integer matchScore;
    private String matchLabel;
    private Double matchConfidence;
    private String matchReason;
    private List<String> matchReasons;
}

