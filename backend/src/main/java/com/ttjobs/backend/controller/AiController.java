package com.ttjobs.backend.controller;

import com.ttjobs.backend.dto.ai.AiNormalizeRequest;
import com.ttjobs.backend.dto.ai.AiSignalDTO;
import com.ttjobs.backend.dto.job.JobDTO;
import com.ttjobs.backend.service.AiNormalizeService;
import com.ttjobs.backend.service.RecommendationService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai")
public class AiController {

    @Autowired
    private AiNormalizeService aiNormalizeService;
    @Autowired
    private RecommendationService recommendationService;

    @PostMapping("/normalize-cv")
    public AiSignalDTO normalizeCv(@RequestBody(required = false) AiNormalizeRequest request) {
        return aiNormalizeService.normalizeCv(request);
    }

    @PostMapping("/normalize-job")
    public AiSignalDTO normalizeJob(@RequestBody AiNormalizeRequest request) {
        return aiNormalizeService.normalizeJob(request);
    }

    @PostMapping("/match-jobs")
    public List<JobDTO> matchJobs() {
        return recommendationService.recommendByJobNeeds();
    }

    @PostMapping("/match-candidates")
    public List<Object> matchCandidates() {
        return List.of();
    }

    @PostMapping("/rerank-matches")
    public List<Object> rerankMatches(@RequestBody(required = false) List<Object> matches) {
        return matches == null ? List.of() : matches;
    }
}
