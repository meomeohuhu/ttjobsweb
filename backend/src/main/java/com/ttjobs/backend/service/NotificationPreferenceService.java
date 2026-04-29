package com.ttjobs.backend.service;

import com.ttjobs.backend.dto.NotificationPreferenceDTO;
import com.ttjobs.backend.dto.NotificationPreferenceRequest;
import com.ttjobs.backend.entity.NotificationPreference;
import com.ttjobs.backend.entity.User;
import com.ttjobs.backend.repository.NotificationPreferenceRepository;
import java.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class NotificationPreferenceService {

    @Autowired
    private NotificationPreferenceRepository preferenceRepository;
    @Autowired
    private AuthContextService authContextService;

    public NotificationPreferenceDTO getMyPreferences() {
        User currentUser = authContextService.requireCurrentUser();
        if (currentUser.getRole() != User.Role.CANDIDATE) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only candidate can manage notification preferences");
        }
        NotificationPreference preference = preferenceRepository.findById(currentUser.getId())
                .orElseGet(() -> createDefault(currentUser.getId()));
        return toDto(preference);
    }

    public NotificationPreferenceDTO updateMyPreferences(NotificationPreferenceRequest request) {
        User currentUser = authContextService.requireCurrentUser();
        if (currentUser.getRole() != User.Role.CANDIDATE) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only candidate can manage notification preferences");
        }
        NotificationPreference preference = preferenceRepository.findById(currentUser.getId())
                .orElseGet(() -> createDefault(currentUser.getId()));

        // Update notification preferences.
        if (request.getInAppEnabled() != null) {
            preference.setInAppEnabled(request.getInAppEnabled());
        }
        if (request.getEmailEnabled() != null) {
            preference.setEmailEnabled(request.getEmailEnabled());
        }
        return toDto(preferenceRepository.save(preference));
    }

    public NotificationPreference getOrDefault(Long userId) {
        return preferenceRepository.findById(userId)
                .orElseGet(() -> createDefault(userId));
    }

    private NotificationPreference createDefault(Long userId) {
        NotificationPreference preference = new NotificationPreference();
        preference.setUserId(userId);
        preference.setInAppEnabled(true);
        preference.setEmailEnabled(false);
        preference.setCreatedAt(LocalDateTime.now());
        return preferenceRepository.save(preference);
    }

    private NotificationPreferenceDTO toDto(NotificationPreference preference) {
        NotificationPreferenceDTO dto = new NotificationPreferenceDTO();
        dto.setInAppEnabled(preference.getInAppEnabled());
        dto.setEmailEnabled(preference.getEmailEnabled());
        return dto;
    }
}
