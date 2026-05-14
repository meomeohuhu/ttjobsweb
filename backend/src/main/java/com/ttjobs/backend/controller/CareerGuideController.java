package com.ttjobs.backend.controller;

import com.ttjobs.backend.dto.career.CareerGuideArticleDTO;
import com.ttjobs.backend.service.CareerGuideService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/career-guides")
public class CareerGuideController {

    @Autowired
    private CareerGuideService careerGuideService;

    @GetMapping
    public List<CareerGuideArticleDTO> getPublishedArticles() {
        return careerGuideService.getPublishedArticles();
    }

    @GetMapping("/{slug}")
    public CareerGuideArticleDTO getArticleBySlug(@PathVariable String slug) {
        return careerGuideService.getArticleBySlug(slug);
    }
}

