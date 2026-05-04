package com.ttjobs.backend.dto;

import lombok.Data;

@Data
public class AiCvScoreRequest {
    private String cvText;
    private String jobText;
}
