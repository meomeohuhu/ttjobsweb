package com.ttjobs.backend.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Data;

@Entity
@Table(name = "notification_preferences")
@Data
public class NotificationPreference {

    @Id
    private Long userId;

    private Boolean inAppEnabled;
    private Boolean emailEnabled;
    private LocalDateTime createdAt;
}
