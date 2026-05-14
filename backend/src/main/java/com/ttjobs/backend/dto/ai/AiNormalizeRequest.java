package com.ttjobs.backend.dto.ai;

import lombok.Data;

@Data
public class AiNormalizeRequest {
    private Long jobId;
    private String text;
}
