package com.ttjobs.backend.dto.notification;

import lombok.Data;

@Data
public class NotificationPreferenceRequest {
    private Boolean inAppEnabled;
    private Boolean emailEnabled;
}

