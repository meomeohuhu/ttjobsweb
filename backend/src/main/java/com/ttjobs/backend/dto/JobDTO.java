package com.ttjobs.backend.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

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
    private String status;
    private LocalDateTime postedDate;
    private LocalDateTime applicationDeadline;
    private Long companyId;
    private String companyName;
}
