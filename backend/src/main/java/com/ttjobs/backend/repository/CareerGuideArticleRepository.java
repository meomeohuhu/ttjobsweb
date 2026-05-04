package com.ttjobs.backend.repository;

import com.ttjobs.backend.entity.CareerGuideArticle;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CareerGuideArticleRepository extends JpaRepository<CareerGuideArticle, Long> {

    List<CareerGuideArticle> findByPublishedAtIsNotNullOrderByFeaturedDescPublishedAtDescIdDesc();

    Optional<CareerGuideArticle> findBySlugAndPublishedAtIsNotNull(String slug);
    Optional<CareerGuideArticle> findBySlug(String slug);
}
