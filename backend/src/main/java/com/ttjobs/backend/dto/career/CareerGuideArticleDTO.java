package com.ttjobs.backend.dto.career;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CareerGuideArticleDTO {

    private Long id;
    private String slug;
    private String title;
    private String summary;
    private String content;
    private String category;
    private String coverImageUrl;
    private Integer readingTimeMinutes;
    private Boolean featured;
    private LocalDateTime publishedAt;
}

