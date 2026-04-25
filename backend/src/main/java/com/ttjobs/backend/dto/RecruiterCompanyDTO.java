package com.ttjobs.backend.dto;

import lombok.Data;

@Data
public class RecruiterCompanyDTO {
    private Long id;
    private String name;
    private String description;
    private String location;
    private String website;
    private String industry;
    private String logoUrl;
    private String memberRole;
    private Long jobCount;
    private Long openJobCount;
    private Long memberCount;
    private Long followerCount;
}
