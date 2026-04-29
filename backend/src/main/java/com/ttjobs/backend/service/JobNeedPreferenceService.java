package com.ttjobs.backend.service;

import com.ttjobs.backend.dto.JobNeedPreferenceDTO;
import com.ttjobs.backend.dto.JobNeedPreferenceRequest;
import com.ttjobs.backend.entity.JobNeedPreference;
import com.ttjobs.backend.entity.User;
import com.ttjobs.backend.repository.JobNeedPreferenceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

@Service
public class JobNeedPreferenceService {

    @Autowired
    private JobNeedPreferenceRepository jobNeedPreferenceRepository;
    @Autowired
    private AuthContextService authContextService;

    public JobNeedPreferenceDTO getMyPreferences() {
        User currentUser = authContextService.requireCurrentUser();
        return toDto(getOrCreate(currentUser.getId()));
    }

    public JobNeedPreferenceDTO updateMyPreferences(JobNeedPreferenceRequest request) {
        User currentUser = authContextService.requireCurrentUser();
        JobNeedPreference preference = getOrCreate(currentUser.getId());

        if (request.getDesiredTitle() != null) {
            preference.setDesiredTitle(normalize(request.getDesiredTitle()));
        }
        if (request.getDesiredLocation() != null) {
            preference.setDesiredLocation(normalize(request.getDesiredLocation()));
        }
        if (request.getDesiredCategory() != null) {
            preference.setDesiredCategory(normalize(request.getDesiredCategory()));
        }
        if (request.getDesiredJobType() != null) {
            preference.setDesiredJobType(normalize(request.getDesiredJobType()));
        }
        if (request.getDesiredExperienceLevel() != null) {
            preference.setDesiredExperienceLevel(normalize(request.getDesiredExperienceLevel()));
        }
        if (request.getMinSalary() != null) {
            preference.setMinSalary(request.getMinSalary());
        }
        if (request.getMaxSalary() != null) {
            preference.setMaxSalary(request.getMaxSalary());
        }
        if (request.getRemoteOnly() != null) {
            preference.setRemoteOnly(request.getRemoteOnly());
        }

        validateSalaryRange(preference);
        return toDto(jobNeedPreferenceRepository.save(preference));
    }

    public JobNeedPreference getOrCreate(Long userId) {
        return jobNeedPreferenceRepository.findById(userId)
                .orElseGet(() -> createDefault(userId));
    }

    public boolean hasConfiguredCriteria(JobNeedPreference preference) {
        return preference != null && (
                hasText(preference.getDesiredTitle())
                || hasText(preference.getDesiredLocation())
                || hasText(preference.getDesiredCategory())
                || hasText(preference.getDesiredJobType())
                || hasText(preference.getDesiredExperienceLevel())
                || preference.getMinSalary() != null
                || preference.getMaxSalary() != null
                || Boolean.TRUE.equals(preference.getRemoteOnly())
        );
    }

    private JobNeedPreference createDefault(Long userId) {
        JobNeedPreference preference = new JobNeedPreference();
        preference.setUserId(userId);
        preference.setRemoteOnly(false);
        preference.setCreatedAt(LocalDateTime.now());
        preference.setUpdatedAt(LocalDateTime.now());
        return jobNeedPreferenceRepository.save(preference);
    }

    private void validateSalaryRange(JobNeedPreference preference) {
        if (preference.getMinSalary() != null && preference.getMaxSalary() != null
                && preference.getMinSalary().compareTo(preference.getMaxSalary()) > 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "minSalary cannot be greater than maxSalary");
        }
    }

    private String normalize(String value) {
        return value == null ? null : value.trim();
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private JobNeedPreferenceDTO toDto(JobNeedPreference preference) {
        JobNeedPreferenceDTO dto = new JobNeedPreferenceDTO();
        dto.setDesiredTitle(preference.getDesiredTitle());
        dto.setDesiredLocation(preference.getDesiredLocation());
        dto.setDesiredCategory(preference.getDesiredCategory());
        dto.setDesiredJobType(preference.getDesiredJobType());
        dto.setDesiredExperienceLevel(preference.getDesiredExperienceLevel());
        dto.setMinSalary(preference.getMinSalary());
        dto.setMaxSalary(preference.getMaxSalary());
        dto.setRemoteOnly(Boolean.TRUE.equals(preference.getRemoteOnly()));
        dto.setConfigured(hasConfiguredCriteria(preference));
        dto.setUpdatedAt(preference.getUpdatedAt());
        return dto;
    }
}
