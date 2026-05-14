package com.ttjobs.backend.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class NotificationPreferenceRequest {
    @NotNull(message = "In-app notification preference is required")
    private Boolean inAppEnabled;

    @NotNull(message = "Email notification preference is required")
    private Boolean emailEnabled;
}
