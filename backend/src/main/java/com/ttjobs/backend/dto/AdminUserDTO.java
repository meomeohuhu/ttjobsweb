package com.ttjobs.backend.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AdminUserDTO {
    private Long id;
    private String name;
    private String email;
    private String role;
    private String phone;
    private LocalDateTime createdAt;
}
