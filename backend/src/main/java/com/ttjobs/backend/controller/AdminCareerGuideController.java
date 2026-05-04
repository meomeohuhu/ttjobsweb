package com.ttjobs.backend.controller;

import com.ttjobs.backend.dto.CareerGuideArticleDTO;
import com.ttjobs.backend.dto.CareerGuideRequest;
import com.ttjobs.backend.service.CareerGuideService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/career-guides")
public class AdminCareerGuideController {

    @Autowired
    private CareerGuideService careerGuideService;

    @PostMapping
    public CareerGuideArticleDTO createArticle(@Valid @RequestBody CareerGuideRequest request) {
        return careerGuideService.createArticle(request);
    }

    @PutMapping("/{id}")
    public CareerGuideArticleDTO updateArticle(@PathVariable Long id, @Valid @RequestBody CareerGuideRequest request) {
        return careerGuideService.updateArticle(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteArticle(@PathVariable Long id) {
        careerGuideService.deleteArticle(id);
    }

    @PutMapping("/{id}/publish")
    public CareerGuideArticleDTO publishArticle(@PathVariable Long id) {
        return careerGuideService.publishArticle(id);
    }
}
