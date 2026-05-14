package com.ttjobs.backend.dto.admin;

import lombok.Data;

@Data
public class AdminPeriodMetricsDTO {
    private String key;
    private String label;
    private long newUsers;
    private long newJobs;
    private long newApplications;
    private long newInterviews;
    private long scheduledInterviews;
}
