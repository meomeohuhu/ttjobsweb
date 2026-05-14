package com.ttjobs.backend.dto.ai;

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

