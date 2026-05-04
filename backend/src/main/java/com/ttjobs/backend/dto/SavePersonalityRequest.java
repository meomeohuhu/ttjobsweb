package com.ttjobs.backend.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SavePersonalityRequest {
    @Pattern(regexp = "^[EI][SN][TF][JP]$", message = "Invalid MBTI type")
    private String mbtiType;
    @Size(max = 8000)
    private String miScoresJson;
    private Boolean personalityPublic;
}
