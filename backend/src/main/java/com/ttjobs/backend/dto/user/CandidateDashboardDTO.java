package com.ttjobs.backend.dto.user;

import com.ttjobs.backend.dto.job.JobDTO;

import com.ttjobs.backend.dto.interview.InterviewScheduleDTO;

import com.ttjobs.backend.dto.application.JobApplicationDTO;

import java.util.List;
import lombok.Data;

@Data
public class CandidateDashboardDTO {
    private long appliedCount;
    private long savedCount;
    private long upcomingInterviewCount;
    private long unreadMessageCount;
    private int profileCompletionPercent;
    private List<String> missingProfileItems;
    private List<JobApplicationDTO> recentApplications;
    private List<JobDTO> recommendedJobs;
    private List<InterviewScheduleDTO> upcomingInterviews;
}

