package com.ttjobs.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AdminRoleUpdateRequest {
    @NotBlank
    private String role;
}
