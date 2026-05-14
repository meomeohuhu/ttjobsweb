package com.ttjobs.backend.dto.admin;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class AdminJobUpdateRequest {
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
    private LocalDateTime applicationDeadline;
    private String reason;
}
