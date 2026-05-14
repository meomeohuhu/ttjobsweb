package com.ttjobs.backend.service;

import com.ttjobs.backend.dto.job.JobNeedPreferenceRequest;
import com.ttjobs.backend.entity.JobNeedPreference;
import com.ttjobs.backend.entity.SavedSearch;
import com.ttjobs.backend.entity.User;
import com.ttjobs.backend.repository.SavedSearchRepository;
import com.ttjobs.backend.repository.UserRepository;
import java.math.BigDecimal;
import java.util.List;
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
    private SavedSearchRepository savedSearchRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuthContextService authContextService;

    @InjectMocks
    private JobNeedPreferenceService jobNeedPreferenceService;

    @Test
    void getMyPreferences_shouldCreateDefaultForCandidate() {
        User candidate = user(1L, User.Role.CANDIDATE);
        when(authContextService.requireCurrentUser()).thenReturn(candidate);
        when(savedSearchRepository.findFirstByUserIdAndNameOrderByUpdatedAtDesc(1L, "Nhu cau viec lam")).thenReturn(Optional.empty());
        when(userRepository.findById(1L)).thenReturn(Optional.of(candidate));
        when(savedSearchRepository.save(any(SavedSearch.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        assertEquals(false, jobNeedPreferenceService.getMyPreferences().getRemoteOnly());
    }

    @Test
    void updateMyPreferences_shouldSaveCriteria() {
        User candidate = user(2L, User.Role.CANDIDATE);
        SavedSearch search = new SavedSearch();
        search.setUser(candidate);
        search.setName("Nhu cau viec lam");
        search.setRemoteOnly(false);

        JobNeedPreferenceRequest request = new JobNeedPreferenceRequest();
        request.setDesiredTitle("Backend Engineer");
        request.setMinSalary(BigDecimal.valueOf(15000000));
        request.setMaxSalary(BigDecimal.valueOf(30000000));
        request.setPreferredSkills(List.of("Java", "Spring Boot"));
        request.setExcludedKeywords(List.of("Intern", "unpaid"));
        request.setRemoteOnly(true);

        when(authContextService.requireCurrentUser()).thenReturn(candidate);
        when(savedSearchRepository.findFirstByUserIdAndNameOrderByUpdatedAtDesc(2L, "Nhu cau viec lam")).thenReturn(Optional.of(search));
        when(savedSearchRepository.save(search)).thenReturn(search);

        var result = jobNeedPreferenceService.updateMyPreferences(request);

        assertEquals("Backend Engineer", result.getDesiredTitle());
        assertEquals(List.of("Java", "Spring Boot"), result.getPreferredSkills());
        assertEquals(List.of("Intern", "unpaid"), result.getExcludedKeywords());
        assertEquals(true, result.getRemoteOnly());
    }

    @Test
    void updateMyPreferences_shouldRejectInvalidSalaryRange() {
        User candidate = user(3L, User.Role.CANDIDATE);
        SavedSearch search = new SavedSearch();
        search.setUser(candidate);
        search.setName("Nhu cau viec lam");

        JobNeedPreferenceRequest request = new JobNeedPreferenceRequest();
        request.setMinSalary(BigDecimal.valueOf(50000000));
        request.setMaxSalary(BigDecimal.valueOf(10000000));

        when(authContextService.requireCurrentUser()).thenReturn(candidate);
        when(savedSearchRepository.findFirstByUserIdAndNameOrderByUpdatedAtDesc(3L, "Nhu cau viec lam")).thenReturn(Optional.of(search));

        assertThrows(ResponseStatusException.class,
                () -> jobNeedPreferenceService.updateMyPreferences(request));
    }

    @Test
    void getMyPreferences_shouldAllowRecruiterAndAdmin() {
        User recruiter = user(4L, User.Role.RECRUITER);
        User admin = user(5L, User.Role.ADMIN);

        when(authContextService.requireCurrentUser()).thenReturn(recruiter);
        when(savedSearchRepository.findFirstByUserIdAndNameOrderByUpdatedAtDesc(4L, "Nhu cau viec lam")).thenReturn(Optional.empty());
        when(userRepository.findById(4L)).thenReturn(Optional.of(recruiter));
        when(savedSearchRepository.save(any(SavedSearch.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        assertEquals(false, jobNeedPreferenceService.getMyPreferences().getRemoteOnly());

        when(authContextService.requireCurrentUser()).thenReturn(admin);
        when(savedSearchRepository.findFirstByUserIdAndNameOrderByUpdatedAtDesc(5L, "Nhu cau viec lam")).thenReturn(Optional.empty());
        when(userRepository.findById(5L)).thenReturn(Optional.of(admin));

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

