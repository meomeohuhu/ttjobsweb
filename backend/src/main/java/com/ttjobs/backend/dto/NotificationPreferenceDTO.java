package com.ttjobs.backend.dto;

import lombok.Data;

@Data
public class NotificationPreferenceDTO {
    private Boolean inAppEnabled;
    private Boolean emailEnabled;
}
