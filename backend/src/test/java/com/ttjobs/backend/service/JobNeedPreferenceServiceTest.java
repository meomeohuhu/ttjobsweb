package com.ttjobs.backend.service;

import com.ttjobs.backend.dto.JobNeedPreferenceRequest;
import com.ttjobs.backend.entity.JobNeedPreference;
import com.ttjobs.backend.entity.User;
import com.ttjobs.backend.repository.JobNeedPreferenceRepository;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JobNeedPreferenceServiceTest {

    @Mock
    private JobNeedPreferenceRepository jobNeedPreferenceRepository;

    @Mock
    private AuthContextService authContextService;

    @InjectMocks
    private JobNeedPreferenceService jobNeedPreferenceService;

    @Test
    void getMyPreferences_shouldCreateDefaultForCandidate() {
        User candidate = user(1L, User.Role.CANDIDATE);
        when(authContextService.requireCurrentUser()).thenReturn(candidate);
        when(jobNeedPreferenceRepository.findById(1L)).thenReturn(Optional.empty());
        when(jobNeedPreferenceRepository.save(any(JobNeedPreference.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        assertEquals(false, jobNeedPreferenceService.getMyPreferences().getRemoteOnly());
    }

    @Test
    void updateMyPreferences_shouldSaveCriteria() {
        User candidate = user(2L, User.Role.CANDIDATE);
        JobNeedPreference preference = new JobNeedPreference();
        preference.setUserId(2L);
        preference.setRemoteOnly(false);

        JobNeedPreferenceRequest request = new JobNeedPreferenceRequest();
        request.setDesiredTitle("Backend Engineer");
        request.setMinSalary(BigDecimal.valueOf(15000000));
        request.setMaxSalary(BigDecimal.valueOf(30000000));
        request.setRemoteOnly(true);

        when(authContextService.requireCurrentUser()).thenReturn(candidate);
        when(jobNeedPreferenceRepository.findById(2L)).thenReturn(Optional.of(preference));
        when(jobNeedPreferenceRepository.save(preference)).thenReturn(preference);

        var result = jobNeedPreferenceService.updateMyPreferences(request);

        assertEquals("Backend Engineer", result.getDesiredTitle());
        assertEquals(true, result.getRemoteOnly());
    }

    @Test
    void updateMyPreferences_shouldRejectInvalidSalaryRange() {
        User candidate = user(3L, User.Role.CANDIDATE);
        JobNeedPreference preference = new JobNeedPreference();
        preference.setUserId(3L);

        JobNeedPreferenceRequest request = new JobNeedPreferenceRequest();
        request.setMinSalary(BigDecimal.valueOf(50000000));
        request.setMaxSalary(BigDecimal.valueOf(10000000));

        when(authContextService.requireCurrentUser()).thenReturn(candidate);
        when(jobNeedPreferenceRepository.findById(3L)).thenReturn(Optional.of(preference));

        assertThrows(ResponseStatusException.class,
                () -> jobNeedPreferenceService.updateMyPreferences(request));
    }

    @Test
    void getMyPreferences_shouldAllowRecruiterAndAdmin() {
        when(authContextService.requireCurrentUser()).thenReturn(user(4L, User.Role.RECRUITER));
        when(jobNeedPreferenceRepository.findById(4L)).thenReturn(Optional.empty());
        when(jobNeedPreferenceRepository.save(any(JobNeedPreference.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        assertEquals(false, jobNeedPreferenceService.getMyPreferences().getRemoteOnly());

        when(authContextService.requireCurrentUser()).thenReturn(user(5L, User.Role.ADMIN));
        when(jobNeedPreferenceRepository.findById(5L)).thenReturn(Optional.empty());

        assertEquals(false, jobNeedPreferenceService.getMyPreferences().getRemoteOnly());
    }

    private User user(Long id, User.Role role) {
        User user = new User();
        user.setId(id);
        user.setRole(role);
        user.setEmail("u" + id + "@mail.com");
        return user;
    }
}
