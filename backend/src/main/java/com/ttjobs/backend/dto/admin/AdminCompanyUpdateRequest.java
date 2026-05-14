package com.ttjobs.backend.dto.admin;

import lombok.Data;

@Data
public class AdminCompanyUpdateRequest {
    private String name;
    private String description;
    private String location;
    private String website;
    private String industry;
    private String verificationStatus;
    private String reason;
}
