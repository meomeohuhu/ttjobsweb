package com.ttjobs.backend.dto.ai;

import lombok.Data;

@Data
public class AiMatchPredictionRequest {
    private String cvText;
    private String jobText;
}
