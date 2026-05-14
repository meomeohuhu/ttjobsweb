package com.ttjobs.backend.dto.recruiter;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;

@Data
public class RecruitmentCampaignRequest {
    private Long companyId;
    private String name;
    private String description;
    private String status;
    private Integer targetHires;
    private LocalDateTime startsAt;
    private LocalDateTime endsAt;
    private List<Long> jobIds;
}

