package com.ttjobs.backend.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class JobApplicationDTO {
    private Long id;
    private LocalDateTime applicationDate;
    private String status;
    private Long userId;
    private String userName;
    private Long jobId;
    private String jobTitle;
    private String companyName;
    private String coverLetter;
    // Flag for UI to show CV availability without exposing URL.
    private boolean hasCv;
}
