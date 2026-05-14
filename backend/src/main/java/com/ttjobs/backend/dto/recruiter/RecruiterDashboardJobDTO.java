package com.ttjobs.backend.dto.recruiter;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RecruiterDashboardJobDTO {
    private Long id;
    private String title;
    private String location;
    private String status;
    private String companyName;
    private String companyLogoUrl;
    private LocalDateTime applicationDeadline;
    private Long applicationCount;
    private Long daysUntilDeadline;
}

