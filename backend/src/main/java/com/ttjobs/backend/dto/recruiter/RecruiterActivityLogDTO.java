package com.ttjobs.backend.dto.recruiter;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RecruiterActivityLogDTO {
    private Long id;
    private String actionType;
    private String title;
    private String details;
    private LocalDateTime createdAt;
    private Long companyId;
    private String companyName;
    private Long jobId;
    private String jobTitle;
    private Long applicationId;
    private String candidateName;
}

