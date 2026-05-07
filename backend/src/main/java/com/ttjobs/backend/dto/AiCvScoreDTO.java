package com.ttjobs.backend.dto;

import lombok.Data;

import java.util.List;

@Data
public class AiCvScoreDTO {
    private Integer score;
    private String level;
    private Double rawScore;
    private List<String> signals;
    private List<String> pros;
    private List<String> cons;
}
