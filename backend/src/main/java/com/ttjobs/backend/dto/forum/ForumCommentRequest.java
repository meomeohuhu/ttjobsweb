package com.ttjobs.backend.dto.forum;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ForumCommentRequest {
    @NotBlank
    @Size(max = 2000)
    private String body;
}

