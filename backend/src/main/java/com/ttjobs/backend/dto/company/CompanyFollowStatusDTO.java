package com.ttjobs.backend.dto.company;

import lombok.Data;

@Data
public class CompanyFollowStatusDTO {
    private Long companyId;
    private boolean followed;
    private Long followerCount;
}

