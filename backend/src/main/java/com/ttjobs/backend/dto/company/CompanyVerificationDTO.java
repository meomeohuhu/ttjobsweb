package com.ttjobs.backend.dto.company;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class CompanyVerificationDTO {
    private Long id;
    private Long companyId;
    private String companyName;
    private String businessLicenseUrl;
    private String taxCode;
    private String website;
    private String note;
    private String status;
    private String reviewReason;
    private Long reviewedById;
    private String reviewedByName;
    private LocalDateTime reviewedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
