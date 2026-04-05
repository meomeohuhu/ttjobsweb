package com.ttjobs.backend.controller;

import com.ttjobs.backend.dto.JobDTO;
import com.ttjobs.backend.service.RecommendationService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/recommendations")
public class RecommendationController {

    @Autowired
    private RecommendationService recommendationService;

    @PostMapping("/cv")
    public List<JobDTO> recommendByCv() {
        return recommendationService.recommendByCv();
    }
}
