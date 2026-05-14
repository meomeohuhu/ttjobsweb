package com.ttjobs.backend.dto.company;

import lombok.Data;

@Data
public class CompanyVerificationRequest {
    private String businessLicenseUrl;
    private String taxCode;
    private String website;
    private String note;
}
