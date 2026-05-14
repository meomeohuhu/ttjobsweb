package com.ttjobs.backend.dto.tool;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ToolSessionRequest {
    @NotBlank
    @Size(max = 60)
    private String toolSlug;
    @NotBlank
    private String inputJson;
    @NotBlank
    private String resultJson;
}

