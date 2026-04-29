package com.ttjobs.backend.dto;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class InterviewScheduleDTO {
    private Long id;
    private Long applicationId;
    private Long candidateId;
    private String candidateName;
    private Long jobId;
    private String jobTitle;
    private Long companyId;
    private String companyName;
    private LocalDateTime scheduledAt;
    private Integer durationMinutes;
    private String location;
    private String meetingLink;
    private String note;
    private String status;
    private LocalDateTime createdAt;
}
