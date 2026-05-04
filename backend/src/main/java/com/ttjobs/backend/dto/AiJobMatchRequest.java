package com.ttjobs.backend.dto;

import lombok.Data;

import java.util.List;

@Data
public class AiJobMatchRequest {
    private String needText;
    private List<AiJobCandidateDTO> jobs;
    private Integer limit;
}
