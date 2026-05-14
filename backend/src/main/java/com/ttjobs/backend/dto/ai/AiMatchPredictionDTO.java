package com.ttjobs.backend.dto.ai;

import lombok.Data;

import java.util.Map;

@Data
public class AiMatchPredictionDTO {
    private String label;
    private Double confidence;
    private Map<String, Double> probabilities;
}
