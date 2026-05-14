package com.ttjobs.backend.dto.admin;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

@Data
public class AdminStatsDTO {
    private long totalUsers;
    private long totalCandidates;
    private long totalRecruiters;
    private long totalAdmins;
    private long totalCompanies;
    private long totalJobs;
    private long totalApplications;
    private long totalInterviews;
    private long pendingInterviews;
    private long upcomingInterviews;
    private long openJobs;
    private long closedJobs;
    private long newUsersLast7Days;
    private long newUsersLast30Days;
    private long storedCandidateMatches;
    private double applicationPerJobRatio;
    private String aiServiceStatus;
    private Boolean aiClassifierReady;
    private Boolean aiMatcherReady;
    private String aiServiceMessage;
    private LocalDateTime aiCheckedAt;
    private Map<String, Long> applicationStatusCounts;
    private Map<String, Long> interviewStatusCounts;
    private Map<String, AdminPeriodMetricsDTO> periodMetrics;
    private AdminPeriodMetricsDTO customPeriodMetrics;
}

