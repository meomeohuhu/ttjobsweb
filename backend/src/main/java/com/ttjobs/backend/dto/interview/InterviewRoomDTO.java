package com.ttjobs.backend.dto.interview;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class InterviewRoomDTO {
    private Long id;
    private Long interviewId;
    private String roomId;
    private String status;
    private LocalDateTime startedAt;
    private LocalDateTime endedAt;
}
