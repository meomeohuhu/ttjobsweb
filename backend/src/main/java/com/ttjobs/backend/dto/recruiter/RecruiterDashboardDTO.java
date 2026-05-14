package com.ttjobs.backend.dto.recruiter;

import com.ttjobs.backend.dto.recruiter.RecruiterActivityLogDTO;

import com.ttjobs.backend.dto.recruiter.RecruiterDashboardJobDTO;

import com.ttjobs.backend.dto.recruiter.RecruiterDashboardCompanyDTO;

import com.ttjobs.backend.dto.application.JobApplicationDTO;

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

