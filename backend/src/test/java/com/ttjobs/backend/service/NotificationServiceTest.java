package com.ttjobs.backend.service;

import com.ttjobs.backend.dto.NotificationDTO;
import com.ttjobs.backend.entity.Notification;
import com.ttjobs.backend.entity.User;
import com.ttjobs.backend.repository.NotificationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;
    @Mock
    private AuthContextService authContextService;

    @InjectMocks
    private NotificationService notificationService;

    @Test
    void getMyNotifications_shouldReturnList() {
        User current = user(1L);
        Notification n = notification(10L, current, false);

        when(authContextService.requireCurrentUser()).thenReturn(current);
        when(notificationRepository.findByUserIdOrderByCreatedAtDesc(1L, PageRequest.of(0, 20)))
                .thenReturn(new PageImpl<>(List.of(n)));

        List<NotificationDTO> result = notificationService.getMyNotifications(0, 20);
        assertEquals(1, result.size());
        assertEquals(10L, result.get(0).getId());
    }

    @Test
    void getUnreadCount_shouldReturnCount() {
        User current = user(1L);
        when(authContextService.requireCurrentUser()).thenReturn(current);
        when(notificationRepository.countByUserIdAndIsReadFalse(1L)).thenReturn(3L);

        assertEquals(3L, notificationService.getUnreadCount().get("unreadCount"));
    }

    @Test
    void markAsRead_shouldReturnNotFound_whenNotificationNotOwned() {
        User current = user(1L);
        when(authContextService.requireCurrentUser()).thenReturn(current);
        when(notificationRepository.findByIdAndUserId(99L, 1L)).thenReturn(java.util.Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> notificationService.markAsRead(99L));
        assertEquals(404, ex.getStatusCode().value());
    }

    @Test
    void createNotification_shouldSave() {
        User target = user(2L);
        Notification n = notification(1L, target, false);
        when(notificationRepository.save(any(Notification.class))).thenReturn(n);

        notificationService.createNotification(target, "t", "c", "x");
    }

    private User user(Long id) {
        User user = new User();
        user.setId(id);
        user.setEmail("u" + id + "@mail.com");
        user.setRole(User.Role.CANDIDATE);
        return user;
    }

    private Notification notification(Long id, User user, boolean isRead) {
        Notification notification = new Notification();
        notification.setId(id);
        notification.setUser(user);
        notification.setTitle("t");
        notification.setContent("c");
        notification.setType("x");
        notification.setIsRead(isRead);
        notification.setCreatedAt(LocalDateTime.now());
        return notification;
    }
}
