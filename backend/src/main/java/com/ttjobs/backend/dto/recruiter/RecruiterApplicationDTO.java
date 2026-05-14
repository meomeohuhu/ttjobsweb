package com.ttjobs.backend.dto.recruiter;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RecruiterApplicationDTO {
    private Long id;
    private LocalDateTime applicationDate;
    private String status;
    private Long candidateId;
    private String candidateName;
    private String candidateEmail;
    private String candidatePhone;
    private Long jobId;
    private String jobTitle;
    private Long companyId;
    private String companyName;
    private boolean hasCv;
    private Integer aiScore;
    private String aiLevel;
    private LocalDateTime aiScoredAt;
    private boolean aiScoreAvailable;
}

