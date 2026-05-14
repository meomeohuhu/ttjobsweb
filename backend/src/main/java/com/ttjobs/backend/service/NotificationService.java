package com.ttjobs.backend.service;

import com.ttjobs.backend.dto.notification.NotificationDTO;
import com.ttjobs.backend.entity.Notification;
import com.ttjobs.backend.entity.User;
import com.ttjobs.backend.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private AuthContextService authContextService;

    public void createNotification(User targetUser, String title, String content, String type) {
        createNotification(targetUser, title, content, type, null);
    }

    public void createNotification(User targetUser, String title, String content, String type, String targetUrl) {
        if (targetUser == null) {
            return;
        }
        Notification notification = new Notification();
        notification.setUser(targetUser);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setType(type);
        notification.setTargetUrl(targetUrl);
        notification.setIsRead(false);
        notification.setCreatedAt(LocalDateTime.now());
        notificationRepository.save(notification);
    }

    public List<NotificationDTO> getMyNotifications(Integer page, Integer size) {
        User currentUser = authContextService.requireCurrentUser();
        int safePage = page == null ? 0 : Math.max(page, 0);
        int safeSize = size == null ? 20 : Math.max(size, 1);
        Pageable pageable = PageRequest.of(safePage, safeSize);

        return notificationRepository.findByUserIdOrderByCreatedAtDesc(currentUser.getId(), pageable)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public Map<String, Long> getUnreadCount() {
        User currentUser = authContextService.requireCurrentUser();
        long count = notificationRepository.countByUserIdAndIsReadFalse(currentUser.getId());
        return Map.of("unreadCount", count);
    }

    public void markAsRead(Long notificationId) {
        User currentUser = authContextService.requireCurrentUser();
        Notification notification = notificationRepository.findByIdAndUserId(notificationId, currentUser.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Notification not found"));
        notification.setIsRead(true);
        notificationRepository.save(notification);
    }

    public void markAllAsRead() {
        User currentUser = authContextService.requireCurrentUser();
        List<Notification> notifications = notificationRepository
                .findByUserIdOrderByCreatedAtDesc(currentUser.getId(), PageRequest.of(0, 1000))
                .getContent();
        for (Notification notification : notifications) {
            if (!Boolean.TRUE.equals(notification.getIsRead())) {
                notification.setIsRead(true);
            }
        }
        notificationRepository.saveAll(notifications);
    }

    private NotificationDTO toDto(Notification notification) {
        NotificationDTO dto = new NotificationDTO();
        dto.setId(notification.getId());
        dto.setTitle(notification.getTitle());
        dto.setContent(notification.getContent());
        dto.setType(notification.getType());
        dto.setTargetUrl(notification.getTargetUrl());
        dto.setIsRead(notification.getIsRead());
        dto.setCreatedAt(notification.getCreatedAt());
        return dto;
    }
}

