package com.ttjobs.backend.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class EmailChangeConfirmRequest {
    @NotBlank(message = "newEmail is required")
    @Email(message = "Invalid email format")
    private String newEmail;

    @NotBlank(message = "code is required")
    private String code;
}

