package com.ttjobs.backend.dto.career;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CareerGuideRequest {
    private String slug;
    @NotBlank
    private String title;
    @NotBlank
    private String summary;
    @NotBlank
    private String content;
    @NotBlank
    private String category;
    private String coverImageUrl;
    private Integer readingTimeMinutes;
    private Boolean featured;
}

