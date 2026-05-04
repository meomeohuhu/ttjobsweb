package com.ttjobs.backend.dto;

import lombok.Data;

@Data
public class AdminStatsDTO {
    private long totalUsers;
    private long totalCandidates;
    private long totalRecruiters;
    private long totalAdmins;
    private long totalCompanies;
    private long totalJobs;
    private long totalApplications;
}
