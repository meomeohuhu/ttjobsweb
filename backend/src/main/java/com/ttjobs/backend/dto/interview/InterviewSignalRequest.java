package com.ttjobs.backend.dto.interview;

import lombok.Data;

@Data
public class InterviewSignalRequest {
    private String type;
    private String payload;
}
