package com.ttjobs.backend.service;

import com.ttjobs.backend.dto.notification.NotificationPreferenceRequest;
import com.ttjobs.backend.entity.NotificationPreference;
import com.ttjobs.backend.entity.User;
import com.ttjobs.backend.repository.NotificationPreferenceRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationPreferenceServiceTest {

    @Mock
    private NotificationPreferenceRepository preferenceRepository;
    @Mock
    private AuthContextService authContextService;

    @InjectMocks
    private NotificationPreferenceService preferenceService;

    @Test
    void getMyPreferences_shouldCreateDefault() {
        User candidate = user(1L);
        when(authContextService.requireCurrentUser()).thenReturn(candidate);
        when(preferenceRepository.findById(1L)).thenReturn(Optional.empty());
        when(preferenceRepository.save(org.mockito.ArgumentMatchers.any(NotificationPreference.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        assertEquals(true, preferenceService.getMyPreferences().getInAppEnabled());
    }

    @Test
    void updateMyPreferences_shouldUpdateFlags() {
        User candidate = user(2L);
        NotificationPreference preference = new NotificationPreference();
        preference.setUserId(2L);
        preference.setInAppEnabled(true);
        preference.setEmailEnabled(false);

        NotificationPreferenceRequest request = new NotificationPreferenceRequest();
        request.setEmailEnabled(true);

        when(authContextService.requireCurrentUser()).thenReturn(candidate);
        when(preferenceRepository.findById(2L)).thenReturn(Optional.of(preference));
        when(preferenceRepository.save(preference)).thenReturn(preference);

        assertEquals(true, preferenceService.updateMyPreferences(request).getEmailEnabled());
    }

    private User user(Long id) {
        User user = new User();
        user.setId(id);
        user.setRole(User.Role.CANDIDATE);
        user.setEmail("u" + id + "@mail.com");
        return user;
    }
}

