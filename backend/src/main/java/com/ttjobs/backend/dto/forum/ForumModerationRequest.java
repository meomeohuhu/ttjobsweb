package com.ttjobs.backend.dto.forum;

import lombok.Data;

@Data
public class ForumModerationRequest {
    private String status;
    private String action;
    private String reason;
    private Boolean hidePost;
    private Boolean hideComment;
}

