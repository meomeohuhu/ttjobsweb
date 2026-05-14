package com.ttjobs.backend.dto;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class RecruiterWorkspaceDTO {
    private List<RecruiterJobDTO> openJobs;
    private Map<String, Long> applicationStatusCounts;
    private List<RecruiterApplicationDTO> recentApplications;
}
