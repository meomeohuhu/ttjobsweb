package com.ttjobs.backend.controller;

import com.ttjobs.backend.dto.NotificationDTO;
import com.ttjobs.backend.dto.NotificationPreferenceDTO;
import com.ttjobs.backend.dto.NotificationPreferenceRequest;
import com.ttjobs.backend.service.NotificationService;
import com.ttjobs.backend.service.NotificationPreferenceService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@Validated
public class NotificationController {

    @Autowired
    private NotificationService notificationService;
    @Autowired
    private NotificationPreferenceService preferenceService;

    @GetMapping
    public List<NotificationDTO> getMyNotifications(
            @RequestParam(required = false, defaultValue = "0") @Min(0) Integer page,
            @RequestParam(required = false, defaultValue = "20") @Min(1) @Max(100) Integer size) {
        return notificationService.getMyNotifications(page, size);
    }

    @GetMapping("/unread-count")
    public Map<String, Long> getUnreadCount() {
        return notificationService.getUnreadCount();
    }

    @PutMapping("/{notificationId}/read")
    public void markAsRead(@PathVariable Long notificationId) {
        notificationService.markAsRead(notificationId);
    }

    @PutMapping("/read-all")
    public void markAllAsRead() {
        notificationService.markAllAsRead();
    }

    @GetMapping("/preferences")
    public NotificationPreferenceDTO getPreferences() {
        return preferenceService.getMyPreferences();
    }

    @PutMapping("/preferences")
    public NotificationPreferenceDTO updatePreferences(@RequestBody NotificationPreferenceRequest request) {
        return preferenceService.updateMyPreferences(request);
    }
}
