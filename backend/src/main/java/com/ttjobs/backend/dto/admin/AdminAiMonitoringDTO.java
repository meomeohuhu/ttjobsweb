package com.ttjobs.backend.dto.admin;

import lombok.Data;

import java.util.Map;

@Data
public class AdminAiMonitoringDTO {
    private String healthStatus;
    private Boolean categoryClassifierReady;
    private Boolean matchClassifierReady;
    private Boolean embeddingMatcherReady;
    private Long requestCount;
    private Long errorCount;
    private Double averageLatencyMs;
    private Long fallbackCount;
    private Map<String, Long> labelDistribution;
    private Map<String, Long> eventDistribution;
    private Double recommendationCtr;
    private Double applyAfterRecommendationRate;
    private Map<String, Long> topMatchedIndustries;
}
