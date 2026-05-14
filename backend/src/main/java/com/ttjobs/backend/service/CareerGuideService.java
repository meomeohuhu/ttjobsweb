package com.ttjobs.backend.service;

import com.ttjobs.backend.dto.career.CareerGuideArticleDTO;
import com.ttjobs.backend.dto.career.CareerGuideRequest;
import com.ttjobs.backend.entity.CareerGuideArticle;
import com.ttjobs.backend.repository.CareerGuideArticleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.time.LocalDateTime;
import java.util.Locale;

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

    public CareerGuideArticleDTO createArticle(CareerGuideRequest request) {
        CareerGuideArticle article = new CareerGuideArticle();
        applyRequest(article, request);
        article.setPublishedAt(null);
        return toDto(careerGuideArticleRepository.save(article));
    }

    public CareerGuideArticleDTO updateArticle(Long id, CareerGuideRequest request) {
        CareerGuideArticle article = careerGuideArticleRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Career guide article not found"));
        applyRequest(article, request);
        return toDto(careerGuideArticleRepository.save(article));
    }

    public void deleteArticle(Long id) {
        if (!careerGuideArticleRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Career guide article not found");
        }
        careerGuideArticleRepository.deleteById(id);
    }

    public CareerGuideArticleDTO publishArticle(Long id) {
        CareerGuideArticle article = careerGuideArticleRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Career guide article not found"));
        article.setPublishedAt(LocalDateTime.now());
        return toDto(careerGuideArticleRepository.save(article));
    }

    private void applyRequest(CareerGuideArticle article, CareerGuideRequest request) {
        String slug = request.getSlug() == null || request.getSlug().isBlank()
                ? slugify(request.getTitle())
                : slugify(request.getSlug());
        article.setSlug(slug);
        article.setTitle(request.getTitle().trim());
        article.setSummary(request.getSummary().trim());
        article.setContent(request.getContent());
        article.setCategory(request.getCategory().trim());
        article.setCoverImageUrl(request.getCoverImageUrl());
        article.setReadingTimeMinutes(request.getReadingTimeMinutes());
        article.setFeatured(Boolean.TRUE.equals(request.getFeatured()));
    }

    private String slugify(String value) {
        String slug = value == null ? "" : value.trim().toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-");
        if (slug.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Slug is required");
        }
        return slug;
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

