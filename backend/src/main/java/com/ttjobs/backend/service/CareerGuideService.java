package com.ttjobs.backend.service;

import com.ttjobs.backend.dto.CareerGuideArticleDTO;
import com.ttjobs.backend.entity.CareerGuideArticle;
import com.ttjobs.backend.repository.CareerGuideArticleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class CareerGuideService {

    @Autowired
    private CareerGuideArticleRepository careerGuideArticleRepository;

    public List<CareerGuideArticleDTO> getPublishedArticles() {
        return careerGuideArticleRepository.findByPublishedAtIsNotNullOrderByFeaturedDescPublishedAtDescIdDesc()
                .stream()
                .map(this::toDto)
                .toList();
    }

    public CareerGuideArticleDTO getArticleBySlug(String slug) {
        if (slug == null || slug.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Slug is required");
        }

        CareerGuideArticle article = careerGuideArticleRepository.findBySlugAndPublishedAtIsNotNull(slug)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Career guide article not found"));
        return toDto(article);
    }

    private CareerGuideArticleDTO toDto(CareerGuideArticle article) {
        CareerGuideArticleDTO dto = new CareerGuideArticleDTO();
        dto.setId(article.getId());
        dto.setSlug(article.getSlug());
        dto.setTitle(article.getTitle());
        dto.setSummary(article.getSummary());
        dto.setContent(article.getContent());
        dto.setCategory(article.getCategory());
        dto.setCoverImageUrl(article.getCoverImageUrl());
        dto.setReadingTimeMinutes(article.getReadingTimeMinutes());
        dto.setFeatured(Boolean.TRUE.equals(article.getFeatured()));
        dto.setPublishedAt(article.getPublishedAt());
        return dto;
    }
}
