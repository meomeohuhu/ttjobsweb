package com.ttjobs.backend.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SavedJobDTO {
    private Long id;
    private Long userId;
    private Long jobId;
    private String jobTitle;
    private String companyName;
    private String jobLocation;
    private String jobStatus;
    private LocalDateTime savedAt;
}
