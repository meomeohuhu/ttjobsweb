package com.ttjobs.backend.dto;

import lombok.Data;

@Data
public class CandidateSearchDTO {
    private Long candidateId;
    private String candidateName;
    private String candidateEmail;
    private String candidatePhone;
    private String address;
    private Integer experienceYears;
    private Long applicationCount;
    private String latestJobTitle;
    private String latestStatus;
    private boolean hasCv;
}
