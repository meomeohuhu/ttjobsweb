package com.ttjobs.backend.dto.user;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PersonalityProfileDTO {
    private Long userId;
    private String mbtiType;
    private LocalDateTime mbtiTakenAt;
    private String miScoresJson;
    private LocalDateTime miTakenAt;
    private Boolean personalityPublic;
}

