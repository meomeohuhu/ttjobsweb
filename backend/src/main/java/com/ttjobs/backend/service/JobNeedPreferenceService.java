package com.ttjobs.backend.service;

import com.ttjobs.backend.dto.job.JobNeedPreferenceDTO;
import com.ttjobs.backend.dto.job.JobNeedPreferenceRequest;
import com.ttjobs.backend.entity.JobNeedPreference;
import com.ttjobs.backend.entity.SavedSearch;
import com.ttjobs.backend.entity.User;
import com.ttjobs.backend.repository.SavedSearchRepository;
import com.ttjobs.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class JobNeedPreferenceService {

    private static final String DEFAULT_NEED_SEARCH_NAME = "Nhu cau viec lam";

    @Autowired
    private SavedSearchRepository savedSearchRepository;
    @Autowired
    private UserRepository userRepository;
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
        if (request.getPreferredSkills() != null) {
            preference.setPreferredSkills(serializeList(request.getPreferredSkills()));
        }
        if (request.getExcludedKeywords() != null) {
            preference.setExcludedKeywords(serializeList(request.getExcludedKeywords()));
        }
        if (request.getRemoteOnly() != null) {
            preference.setRemoteOnly(request.getRemoteOnly());
        }

        validateSalaryRange(preference);
        SavedSearch search = getOrCreateDefaultSearch(currentUser.getId());
        applyPreference(search, preference);
        return toDto(toPreference(savedSearchRepository.save(search)));
    }

    public JobNeedPreference getOrCreate(Long userId) {
        return toPreference(getOrCreateDefaultSearch(userId));
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
                || hasText(preference.getPreferredSkills())
                || hasText(preference.getExcludedKeywords())
                || Boolean.TRUE.equals(preference.getRemoteOnly())
        );
    }

    private SavedSearch getOrCreateDefaultSearch(Long userId) {
        return savedSearchRepository.findFirstByUserIdAndNameOrderByUpdatedAtDesc(userId, DEFAULT_NEED_SEARCH_NAME)
                .orElseGet(() -> createDefaultSearch(userId));
    }

    private SavedSearch createDefaultSearch(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        SavedSearch search = new SavedSearch();
        search.setUser(user);
        search.setName(DEFAULT_NEED_SEARCH_NAME);
        search.setRemoteOnly(false);
        search.setActive(true);
        search.setAlertFrequency("DAILY");
        search.setCreatedAt(LocalDateTime.now());
        search.setUpdatedAt(LocalDateTime.now());
        return savedSearchRepository.save(search);
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

    private String serializeList(List<String> values) {
        if (values == null) {
            return null;
        }
        String serialized = values.stream()
                .filter(this::hasText)
                .map(String::trim)
                .distinct()
                .collect(Collectors.joining(","));
        return serialized.isBlank() ? null : serialized;
    }

    public List<String> deserializeList(String value) {
        if (!hasText(value)) {
            return List.of();
        }
        return Arrays.stream(value.split(","))
                .map(String::trim)
                .filter(this::hasText)
                .toList();
    }

    private void applyPreference(SavedSearch search, JobNeedPreference preference) {
        search.setKeyword(preference.getDesiredTitle());
        search.setLocation(preference.getDesiredLocation());
        search.setCategory(preference.getDesiredCategory());
        search.setJobType(preference.getDesiredJobType());
        search.setExperienceLevel(preference.getDesiredExperienceLevel());
        search.setSalaryMin(preference.getMinSalary());
        search.setSalaryMax(preference.getMaxSalary());
        search.setSkills(preference.getPreferredSkills());
        search.setExcludedKeywords(preference.getExcludedKeywords());
        search.setRemoteOnly(Boolean.TRUE.equals(preference.getRemoteOnly()));
        search.setActive(true);
        if (!hasText(search.getAlertFrequency())) {
            search.setAlertFrequency("DAILY");
        }
    }

    private JobNeedPreference toPreference(SavedSearch search) {
        JobNeedPreference preference = new JobNeedPreference();
        preference.setUserId(search.getUser() == null ? null : search.getUser().getId());
        preference.setDesiredTitle(search.getKeyword());
        preference.setDesiredLocation(search.getLocation());
        preference.setDesiredCategory(search.getCategory());
        preference.setDesiredJobType(search.getJobType());
        preference.setDesiredExperienceLevel(search.getExperienceLevel());
        preference.setMinSalary(search.getSalaryMin());
        preference.setMaxSalary(search.getSalaryMax());
        preference.setPreferredSkills(search.getSkills());
        preference.setExcludedKeywords(search.getExcludedKeywords());
        preference.setRemoteOnly(Boolean.TRUE.equals(search.getRemoteOnly()));
        preference.setCreatedAt(search.getCreatedAt());
        preference.setUpdatedAt(search.getUpdatedAt());
        return preference;
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
        dto.setPreferredSkills(deserializeList(preference.getPreferredSkills()));
        dto.setExcludedKeywords(deserializeList(preference.getExcludedKeywords()));
        dto.setRemoteOnly(Boolean.TRUE.equals(preference.getRemoteOnly()));
        dto.setConfigured(hasConfiguredCriteria(preference));
        dto.setUpdatedAt(preference.getUpdatedAt());
        return dto;
    }
}

