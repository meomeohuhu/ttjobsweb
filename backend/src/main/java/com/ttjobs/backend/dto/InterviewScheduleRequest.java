package com.ttjobs.backend.dto;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class InterviewScheduleRequest {
    private Long applicationId;
    private LocalDateTime scheduledAt;
    private Integer durationMinutes;
    private String location;
    private String meetingLink;
    private String note;
    private String status;
}
