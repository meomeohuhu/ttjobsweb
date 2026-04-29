package com.ttjobs.backend.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class RecruiterDashboardDTO {
    private Long openJobCount;
    private Long newApplicationCount;
    private Long expiringSoonJobCount;
    private Map<String, Long> applicationStatusCounts;
    private List<RecruiterDashboardJobDTO> expiringSoonJobs;
    private List<JobApplicationDTO> recentApplications;
    private List<RecruiterDashboardCompanyDTO> managedCompanies;
    private List<RecruiterActivityLogDTO> recentActivities;
}
