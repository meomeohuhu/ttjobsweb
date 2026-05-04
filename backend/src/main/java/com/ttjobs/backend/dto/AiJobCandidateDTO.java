package com.ttjobs.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AiJobCandidateDTO {
    private Long jobId;
    private String text;
}
