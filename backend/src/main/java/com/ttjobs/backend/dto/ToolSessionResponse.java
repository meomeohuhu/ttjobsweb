package com.ttjobs.backend.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ToolSessionResponse {
    private Long id;
    private String toolSlug;
    private String inputJson;
    private String resultJson;
    private LocalDateTime createdAt;
}
