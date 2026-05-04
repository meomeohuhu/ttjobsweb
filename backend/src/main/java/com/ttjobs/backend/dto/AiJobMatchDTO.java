package com.ttjobs.backend.dto;

import lombok.Data;

import java.util.List;

@Data
public class AiJobMatchDTO {
    private Long jobId;
    private Integer score;
    private List<String> reasons;
}
