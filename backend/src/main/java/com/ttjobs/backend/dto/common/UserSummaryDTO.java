package com.ttjobs.backend.dto.common;

import lombok.Data;

@Data
public class UserSummaryDTO {
    private Long id;
    private String name;
    private String email;
    private String role;
    private String avatarUrl;
}
