package com.ttjobs.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class EmailChangeRequest {
    @NotBlank(message = "newEmail is required")
    @Email(message = "Invalid email format")
    private String newEmail;
}
