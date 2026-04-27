package com.ttjobs.backend.dto;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;

@Data
public class RecruitmentCampaignDTO {
    private Long id;
    private Long companyId;
    private String companyName;
    private String name;
    private String description;
    private String status;
    private Integer targetHires;
    private LocalDateTime startsAt;
    private LocalDateTime endsAt;
    private Long jobCount;
    private Long applicationCount;
    private List<Long> jobIds;
    private LocalDateTime createdAt;
}
