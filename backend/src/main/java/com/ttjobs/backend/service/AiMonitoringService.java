package com.ttjobs.backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.ttjobs.backend.dto.admin.AdminAiMonitoringDTO;
import com.ttjobs.backend.dto.admin.AiMatchEventDTO;
import com.ttjobs.backend.entity.AiMatchEvent;
import com.ttjobs.backend.entity.AiServiceCallLog;
import com.ttjobs.backend.entity.Job;
import com.ttjobs.backend.repository.AiMatchEventRepository;
import com.ttjobs.backend.repository.AiServiceCallLogRepository;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class AiMonitoringService {

    private static final Logger log = LoggerFactory.getLogger(AiMonitoringService.class);
    private static final int SNAPSHOT_LIMIT = 4_000;

    private final AiMatchEventRepository matchEventRepository;
    private final AiServiceCallLogRepository callLogRepository;
    private final RestTemplate restTemplate;
    private final String aiBaseUrl;

    public AiMonitoringService(
            AiMatchEventRepository matchEventRepository,
            AiServiceCallLogRepository callLogRepository,
            RestTemplate restTemplate,
            @Value("${ttjobs.ai.base-url}") String aiBaseUrl) {
        this.matchEventRepository = matchEventRepository;
        this.callLogRepository = callLogRepository;
        this.restTemplate = restTemplate;
        this.aiBaseUrl = aiBaseUrl;
    }

    public void recordAiCall(String endpoint, String status, Integer httpStatus, long latencyMs,
                             boolean fallbackUsed, String predictedLabel, Double confidence) {
        try {
            AiServiceCallLog entry = new AiServiceCallLog();
            entry.setEndpoint(endpoint);
            entry.setStatus(status);
            entry.setHttpStatus(httpStatus);
            entry.setLatencyMs(Math.max(0, latencyMs));
            entry.setFallbackUsed(fallbackUsed);
            entry.setPredictedLabel(predictedLabel);
            entry.setConfidence(confidence);
            entry.setCreatedAt(LocalDateTime.now());
            callLogRepository.save(entry);
        } catch (Exception ex) {
            log.warn("Failed to persist AI call log: {}", ex.getMessage());
        }
    }

    public void recordMatchEvent(Long userId, Job job, String eventType, String cvText,
                                 String jobText, String predictedLabel, Integer predictedScore, String source) {
        try {
            AiMatchEvent event = new AiMatchEvent();
            event.setUserId(userId);
            event.setJobId(job == null ? null : job.getId());
            event.setEventType(eventType);
            event.setCvSnapshotText(truncate(cvText));
            event.setJobSnapshotText(truncate(jobText));
            event.setPredictedLabel(predictedLabel);
            event.setPredictedScore(predictedScore);
            event.setSource(source);
            event.setCreatedAt(LocalDateTime.now());
            matchEventRepository.save(event);
        } catch (Exception ex) {
            log.warn("Failed to persist AI match event: {}", ex.getMessage());
        }
    }

    public AdminAiMonitoringDTO getMonitoring() {
        AdminAiMonitoringDTO dto = new AdminAiMonitoringDTO();
        applyHealth(dto);
        dto.setRequestCount(callLogRepository.count());
        dto.setErrorCount(callLogRepository.countByStatus("ERROR"));
        dto.setAverageLatencyMs(callLogRepository.averageLatencyMs());
        dto.setFallbackCount(callLogRepository.countByFallbackUsedTrue());
        dto.setLabelDistribution(toMap(callLogRepository.countByPredictedLabelGroup()));
        dto.setEventDistribution(toMap(matchEventRepository.countByEventTypeGroup()));
        dto.setRecommendationCtr(rate(
                matchEventRepository.countByEventType("recommendation_clicked"),
                matchEventRepository.countByEventType("recommendation_shown")));
        dto.setApplyAfterRecommendationRate(rate(
                matchEventRepository.countByEventType("application_created"),
                matchEventRepository.countByEventType("recommendation_shown")));
        dto.setTopMatchedIndustries(toMap(matchEventRepository.topMatchedIndustries()));
        return dto;
    }

    public List<AiMatchEventDTO> getTrainingEvents(int size) {
        int safeSize = Math.max(1, Math.min(size, 500));
        return matchEventRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(0, safeSize))
                .stream()
                .map(this::toDto)
                .toList();
    }

    public List<AiMatchEventDTO> getTrainingEvents(
            int size,
            String eventType,
            String label,
            LocalDateTime from,
            LocalDateTime to,
            Integer minScore,
            Integer maxScore) {
        int safeSize = Math.max(1, Math.min(size, 1000));
        return matchEventRepository.searchTrainingEvents(
                        blankToNull(eventType),
                        blankToNull(label),
                        from,
                        to,
                        minScore,
                        maxScore,
                        PageRequest.of(0, safeSize))
                .stream()
                .map(this::toDto)
                .toList();
    }

    private void applyHealth(AdminAiMonitoringDTO dto) {
        try {
            JsonNode health = restTemplate.getForObject(aiBaseUrl + "/health", JsonNode.class);
            JsonNode source = health == null ? null : health.has("detail") ? health.get("detail") : health;
            dto.setHealthStatus(source == null ? "unknown" : source.path("status").asText("ok"));
            dto.setCategoryClassifierReady(source != null && source.path("categoryClassifierReady").asBoolean(false));
            dto.setMatchClassifierReady(source != null && source.path("matchClassifierReady").asBoolean(false));
            dto.setEmbeddingMatcherReady(source != null && source.path("embeddingMatcherReady").asBoolean(false));
        } catch (Exception ex) {
            dto.setHealthStatus("down");
            dto.setCategoryClassifierReady(false);
            dto.setMatchClassifierReady(false);
            dto.setEmbeddingMatcherReady(false);
        }
    }

    private AiMatchEventDTO toDto(AiMatchEvent event) {
        AiMatchEventDTO dto = new AiMatchEventDTO();
        dto.setId(event.getId());
        dto.setUserId(event.getUserId());
        dto.setJobId(event.getJobId());
        dto.setEventType(event.getEventType());
        dto.setCvSnapshotText(event.getCvSnapshotText());
        dto.setJobSnapshotText(event.getJobSnapshotText());
        dto.setPredictedLabel(event.getPredictedLabel());
        dto.setPredictedScore(event.getPredictedScore());
        dto.setSource(event.getSource());
        dto.setCreatedAt(event.getCreatedAt());
        return dto;
    }

    private Map<String, Long> toMap(List<Object[]> rows) {
        Map<String, Long> result = new LinkedHashMap<>();
        for (Object[] row : rows) {
            String key = row[0] == null ? "UNKNOWN" : row[0].toString();
            Long value = row[1] instanceof Number number ? number.longValue() : 0L;
            result.put(key, value);
        }
        return result;
    }

    private Double rate(long numerator, long denominator) {
        if (denominator <= 0) {
            return 0.0;
        }
        return Math.round((numerator * 10000.0) / denominator) / 100.0;
    }

    private String truncate(String value) {
        if (value == null) {
            return null;
        }
        return value.length() <= SNAPSHOT_LIMIT ? value : value.substring(0, SNAPSHOT_LIMIT);
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
