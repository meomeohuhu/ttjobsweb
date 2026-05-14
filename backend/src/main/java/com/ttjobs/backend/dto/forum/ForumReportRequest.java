package com.ttjobs.backend.dto.forum;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ForumReportRequest {
    private Long postId;
    private Long commentId;

    @NotBlank
    @Size(max = 255)
    private String reason;

    @Size(max = 2000)
    private String details;
}

