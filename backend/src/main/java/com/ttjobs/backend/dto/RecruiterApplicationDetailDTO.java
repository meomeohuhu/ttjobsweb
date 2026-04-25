package com.ttjobs.backend.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class RecruiterApplicationDetailDTO {
    private Long id;
    private LocalDateTime applicationDate;
    private String status;
    private Long candidateId;
    private String candidateName;
    private String candidateEmail;
    private String candidatePhone;
    private String candidateAddress;
    private Integer candidateExperienceYears;
    private Long jobId;
    private String jobTitle;
    private String jobStatus;
    private Long companyId;
    private String companyName;
    private boolean hasCv;
    private List<ApplicationTimelineDTO> timeline;
}
