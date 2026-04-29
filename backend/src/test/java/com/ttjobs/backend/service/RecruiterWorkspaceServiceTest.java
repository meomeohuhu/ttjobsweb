package com.ttjobs.backend.service;

import com.ttjobs.backend.dto.RecruiterWorkspaceDTO;
import com.ttjobs.backend.entity.Company;
import com.ttjobs.backend.entity.Job;
import com.ttjobs.backend.entity.JobApplication;
import com.ttjobs.backend.entity.User;
import com.ttjobs.backend.repository.JobApplicationRepository;
import com.ttjobs.backend.repository.JobRepository;
import com.ttjobs.backend.entity.CompanyMember;
import com.ttjobs.backend.repository.SavedJobRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RecruiterWorkspaceServiceTest {

    @Mock
    private AuthContextService authContextService;
    @Mock
    private JobRepository jobRepository;
    @Mock
    private JobApplicationRepository jobApplicationRepository;
    @Mock
    private SavedJobRepository savedJobRepository;

    @InjectMocks
    private RecruiterWorkspaceService recruiterWorkspaceService;

    @Test
    void getWorkspaceSummary_shouldReturnData_whenUserIsRecruiter() {
        User recruiter = new User();
        recruiter.setId(1L);
        recruiter.setRole(User.Role.RECRUITER);
        recruiter.setEmail("recruiter@example.com");

        Company company = new Company();
        company.setId(10L);
        company.setName("Test Company");

        Job openJob = new Job();
        openJob.setId(100L);
        openJob.setTitle("Software Engineer");
        openJob.setStatus("open");
        openJob.setCompany(company);

        JobApplication application = new JobApplication();
        application.setId(1000L);
        application.setJob(openJob);
        application.setStatus("submitted");
        application.setApplicationDate(LocalDateTime.now());

        when(authContextService.requireCurrentUser()).thenReturn(recruiter);
        // loadManagedJobs
        when(jobRepository.findManagedJobsByRecruiterId(1L, List.of(CompanyMember.MemberRole.RECRUITER, CompanyMember.MemberRole.ADMIN)))
                .thenReturn(List.of(openJob));
        // loadManagedApplications
        when(jobApplicationRepository.findByJobIdIn(List.of(100L))).thenReturn(List.of(application));

        RecruiterWorkspaceDTO result = recruiterWorkspaceService.getWorkspaceSummary();

        assertNotNull(result);
        assertEquals(1, result.getOpenJobs().size());
        assertEquals("Software Engineer", result.getOpenJobs().get(0).getTitle());
        assertEquals(1, result.getApplicationStatusCounts().get("submitted"));
        assertEquals(1, result.getRecentApplications().size());
    }

    @Test
    void getWorkspaceSummary_shouldThrowForbidden_whenUserIsCandidate() {
        User candidate = new User();
        candidate.setId(2L);
        candidate.setRole(User.Role.CANDIDATE);
        candidate.setEmail("candidate@example.com");

        when(authContextService.requireCurrentUser()).thenReturn(candidate);

        assertThrows(ResponseStatusException.class, () -> recruiterWorkspaceService.getWorkspaceSummary());
    }
}
