package com.ttjobs.backend.dto.company;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CompanyMemberDTO {
    private Long id;
    private Long companyId;
    private Long userId;
    private String userName;
    private String userEmail;
    private String memberRole;
    private LocalDateTime createdAt;
}

