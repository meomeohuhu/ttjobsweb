package com.ttjobs.backend.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class NotificationDTO {
    private Long id;
    private String title;
    private String content;
    private String type;
    private String targetUrl;
    private Boolean isRead;
    private LocalDateTime createdAt;
}
