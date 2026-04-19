package com.ttjobs.backend.dto;

import lombok.Data;

@Data
public class CompanyFollowStatusDTO {
    private Long companyId;
    private boolean followed;
    private Long followerCount;
}
