package com.ttjobs.backend.dto.company;

import lombok.Data;

@Data
public class CompanyDTO {
    private Long id;
    private String name;
    private String description;
    private String location;
    private String website;
    private String industry;
    private String logoUrl;
    private String verificationStatus;
    private Long jobCount;
    private Long savedJobCount;
    private Long followerCount;
}

