package com.ttjobs.backend.controller;

import com.ttjobs.backend.dto.NotificationDTO;
import com.ttjobs.backend.service.JwtService;
import com.ttjobs.backend.service.NotificationPreferenceService;
import com.ttjobs.backend.service.NotificationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(NotificationController.class)
@AutoConfigureMockMvc(addFilters = false)
class NotificationControllerApiTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private NotificationService notificationService;
    @MockBean
    private JwtService jwtService;
    @MockBean
    private NotificationPreferenceService notificationPreferenceService;

    @Test
    void getMyNotifications_shouldReturnList() throws Exception {
        NotificationDTO dto = new NotificationDTO();
        dto.setId(1L);
        dto.setTitle("Application submitted");
        dto.setIsRead(false);
        dto.setCreatedAt(LocalDateTime.now());

        when(notificationService.getMyNotifications(0, 20)).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/notifications")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].isRead").value(false));
    }

    @Test
    void getUnreadCount_shouldReturnCount() throws Exception {
        when(notificationService.getUnreadCount()).thenReturn(Map.of("unreadCount", 5L));

        mockMvc.perform(get("/api/notifications/unread-count"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.unreadCount").value(5));
    }

    @Test
    void markAsRead_shouldReturnOk() throws Exception {
        doNothing().when(notificationService).markAsRead(eq(1L));

        mockMvc.perform(put("/api/notifications/1/read"))
                .andExpect(status().isOk());
    }

    @Test
    void markAllAsRead_shouldReturnOk() throws Exception {
        doNothing().when(notificationService).markAllAsRead();

        mockMvc.perform(put("/api/notifications/read-all"))
                .andExpect(status().isOk());
    }
}
