package com.ttjobs.backend.dto;

import lombok.Data;

@Data
public class NotificationPreferenceRequest {
    private Boolean inAppEnabled;
    private Boolean emailEnabled;
}
