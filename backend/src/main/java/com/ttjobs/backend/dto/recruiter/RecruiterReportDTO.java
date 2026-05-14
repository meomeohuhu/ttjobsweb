package com.ttjobs.backend.dto.recruiter;

import java.util.Map;
import lombok.Data;

@Data
public class RecruiterReportDTO {
    private Long openJobs;
    private Long totalApplications;
    private Long newApplications;
    private Long interviewsScheduled;
    private Long hiredApplications;
    private Long rejectedApplications;
    private Map<String, Long> applicationsByStatus;
    private Map<String, Long> applicationsByJob;
}

