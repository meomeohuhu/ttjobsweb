package com.ttjobs.backend.dto.ai;

import lombok.Data;

import java.util.List;

@Data
public class AiJobMatchDTO {
    private Long jobId;
    private Integer score;
    private String matchLabel;
    private Double matchConfidence;
    private List<String> reasons;
}

