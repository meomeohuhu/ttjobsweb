package com.ttjobs.backend.dto.ai;

import lombok.Data;

@Data
public class AiCvScoreRequest {
    private String cvText;
    private String jobText;
}

