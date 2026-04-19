package com.ttjobs.backend.dto;

import lombok.Data;

@Data
public class RecruiterDashboardCompanyDTO {
    private Long companyId;
    private String companyName;
    private String companyLogoUrl;
    private Long jobCount;
    private Long memberCount;
}
