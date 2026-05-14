package com.ttjobs.backend.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ttjobs.backend.dto.job.JobDTO;
import com.ttjobs.backend.service.RecommendationService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("/api/recommendations")
public class RecommendationController {

    @Autowired
    private RecommendationService recommendationService;
    @Autowired
    private ObjectMapper objectMapper;

    @PostMapping("/cv")
    public List<JobDTO> recommendByCv() {
        return recommendationService.recommendByCv();
    }

    @GetMapping("/job-needs")
    public List<JobDTO> recommendByJobNeeds() {
        return recommendationService.recommendByJobNeeds();
    }

    @PostMapping(value = "/cv-text", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.TEXT_PLAIN_VALUE, MediaType.ALL_VALUE})
    public List<JobDTO> recommendByCvText(@RequestBody(required = false) String body) {
        String cvText = extractCvText(body);
        if (cvText == null || cvText.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "cvText is required");
        }
        return recommendationService.recommendByCvText(cvText);
    }

    @PostMapping("/jobs/{jobId}/event")
    public void recordRecommendationEvent(@PathVariable Long jobId, @RequestParam String eventType) {
        recommendationService.recordRecommendationInteraction(jobId, eventType);
    }

    private String extractCvText(String body) {
        if (body == null) {
            return null;
        }

        String trimmed = body.trim();
        if (trimmed.isEmpty()) {
            return null;
        }

        if (trimmed.startsWith("{") || trimmed.startsWith("[")) {
            try {
                JsonNode node = objectMapper.readTree(trimmed);
                String cvText = firstText(node, "cvText");
                if (cvText != null && !cvText.isBlank()) {
                    return cvText;
                }
                cvText = firstText(node, "message");
                if (cvText != null && !cvText.isBlank()) {
                    return cvText;
                }
                cvText = firstText(node, "text");
                if (cvText != null && !cvText.isBlank()) {
                    return cvText;
                }
                return firstText(node, "content");
            } catch (Exception ignored) {
                // Fall through to treat it as raw text.
            }
        }

        try {
            return objectMapper.readValue(trimmed, String.class);
        } catch (Exception ignored) {
            return trimmed;
        }
    }

    private String firstText(JsonNode node, String fieldName) {
        JsonNode field = node.path(fieldName);
        if (field.isMissingNode() || field.isNull()) {
            return null;
        }
        String value = field.asText(null);
        return value == null ? null : value.trim();
    }
}

