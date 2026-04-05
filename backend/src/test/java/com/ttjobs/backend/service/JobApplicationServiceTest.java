package com.ttjobs.backend.service;

import com.ttjobs.backend.dto.JobApplicationDTO;
import com.ttjobs.backend.entity.Company;
import com.ttjobs.backend.entity.Job;
import com.ttjobs.backend.entity.JobApplication;
import com.ttjobs.backend.entity.User;
import com.ttjobs.backend.entity.JobApplicationStatusAudit;
import com.ttjobs.backend.repository.JobApplicationRepository;
import com.ttjobs.backend.repository.JobApplicationStatusAuditRepository;
import com.ttjobs.backend.repository.JobRepository;
import com.ttjobs.backend.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class JobApplicationServiceTest {

    @Mock
    private JobApplicationRepository jobApplicationRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private JobRepository jobRepository;
    @Mock
    private AuthContextService authContextService;
    @Mock
    private CompanyAuthorizationService companyAuthorizationService;
    @Mock
    private JobApplicationStatusAuditRepository statusAuditRepository;
    @Mock
    private NotificationService notificationService;
    @Mock
    private EmailService emailService;

    @InjectMocks
    private JobApplicationService jobApplicationService;

    @Test
    void applyForJob_shouldReturnConflict_whenDuplicateApplication() {
        User currentUser = user(1L, User.Role.CANDIDATE);
        when(authContextService.requireCurrentUser()).thenReturn(currentUser);
        when(jobApplicationRepository.findByUserIdAndJobId(1L, 10L))
                .thenReturn(Optional.of(new JobApplication()));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> jobApplicationService.applyForJob(1L, 10L));

        assertEquals(409, ex.getStatusCode().value());
    }

    @Test
    void applyForJob_shouldReturnBadRequest_whenJobIsClosed() {
        User currentUser = user(1L, User.Role.CANDIDATE);
        Job closedJob = new Job();
        closedJob.setStatus("closed");

        when(authContextService.requireCurrentUser()).thenReturn(currentUser);
        when(jobApplicationRepository.findByUserIdAndJobId(1L, 10L)).thenReturn(Optional.empty());
        when(userRepository.findById(1L)).thenReturn(Optional.of(currentUser));
        when(jobRepository.findByIdAndDeletedAtIsNull(10L)).thenReturn(Optional.of(closedJob));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> jobApplicationService.applyForJob(1L, 10L));

        assertEquals(400, ex.getStatusCode().value());
    }

    @Test
    void applyForJob_shouldSendEmail_whenSuccess() {
        User currentUser = user(1L, User.Role.CANDIDATE);
        User recruiter = user(2L, User.Role.RECRUITER);
        Company company = new Company();
        company.setCreatedBy(recruiter);

        Job job = new Job();
        job.setId(10L);
        job.setTitle("Java Dev");
        job.setStatus("open");
        job.setCompany(company);

        JobApplication application = new JobApplication();
        application.setId(77L);
        application.setUser(currentUser);
        application.setJob(job);
        application.setStatus("submitted");
        application.setApplicationDate(LocalDateTime.now());

        when(authContextService.requireCurrentUser()).thenReturn(currentUser);
        when(jobApplicationRepository.findByUserIdAndJobId(1L, 10L)).thenReturn(Optional.empty());
        when(userRepository.findById(1L)).thenReturn(Optional.of(currentUser));
        when(jobRepository.findByIdAndDeletedAtIsNull(10L)).thenReturn(Optional.of(job));
        when(jobApplicationRepository.save(org.mockito.ArgumentMatchers.any(JobApplication.class))).thenReturn(application);

        JobApplicationDTO result = jobApplicationService.applyForJob(1L, 10L);
        assertEquals(77L, result.getId());
        verify(emailService).sendApplicationSubmitted(currentUser, job);
        verify(emailService).sendNewApplication(recruiter, currentUser, job);
    }

    @Test
    void updateApplicationStatus_shouldReturnBadRequest_whenTransitionInvalid() {
        User recruiter = user(2L, User.Role.RECRUITER);
        Company company = new Company();
        company.setCreatedBy(recruiter);

        Job job = new Job();
        job.setCompany(company);

        JobApplication application = new JobApplication();
        application.setId(5L);
        application.setJob(job);
        application.setStatus("submitted");

        when(authContextService.requireCurrentUser()).thenReturn(recruiter);
        when(authContextService.isAdmin(recruiter)).thenReturn(false);
        when(jobApplicationRepository.findById(5L)).thenReturn(Optional.of(application));
        when(companyAuthorizationService.canManageCompany(recruiter, company)).thenReturn(true);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> jobApplicationService.updateApplicationStatus(5L, "offered"));

        assertEquals(400, ex.getStatusCode().value());
    }

    @Test
    void deleteApplication_shouldReturnForbidden_whenCandidateDeletesOthersApplication() {
        User currentUser = user(2L, User.Role.CANDIDATE);
        User owner = user(1L, User.Role.CANDIDATE);

        JobApplication application = new JobApplication();
        application.setId(6L);
        application.setUser(owner);
        application.setStatus("submitted");
        application.setApplicationDate(LocalDateTime.now());

        when(authContextService.requireCurrentUser()).thenReturn(currentUser);
        when(jobApplicationRepository.findById(6L)).thenReturn(Optional.of(application));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> jobApplicationService.deleteApplication(6L));

        assertEquals(403, ex.getStatusCode().value());
    }

    @Test
    void applyForJob_shouldReturnForbidden_whenUserAppliesForAnotherUser() {
        User currentUser = user(2L, User.Role.CANDIDATE);
        when(authContextService.requireCurrentUser()).thenReturn(currentUser);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> jobApplicationService.applyForJob(1L, 10L));

        assertEquals(403, ex.getStatusCode().value());
    }

    @Test
    void updateApplicationStatus_shouldReturnForbidden_whenRecruiterDoesNotOwnJob() {
        User recruiter = user(10L, User.Role.RECRUITER);
        User owner = user(1L, User.Role.RECRUITER);

        Company company = new Company();
        company.setCreatedBy(owner);

        Job job = new Job();
        job.setCompany(company);

        JobApplication application = new JobApplication();
        application.setId(100L);
        application.setJob(job);
        application.setStatus("submitted");

        when(authContextService.requireCurrentUser()).thenReturn(recruiter);
        when(authContextService.isAdmin(recruiter)).thenReturn(false);
        when(jobApplicationRepository.findById(100L)).thenReturn(Optional.of(application));
        when(companyAuthorizationService.canManageCompany(recruiter, company)).thenReturn(false);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> jobApplicationService.updateApplicationStatus(100L, "reviewing"));

        assertEquals(403, ex.getStatusCode().value());
    }

    @Test
    void deleteApplication_shouldReturnBadRequest_whenApplicationIsTerminal() {
        User currentUser = user(1L, User.Role.CANDIDATE);

        JobApplication application = new JobApplication();
        application.setId(6L);
        application.setUser(currentUser);
        application.setStatus("hired");

        when(authContextService.requireCurrentUser()).thenReturn(currentUser);
        when(jobApplicationRepository.findById(6L)).thenReturn(Optional.of(application));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> jobApplicationService.deleteApplication(6L));

        assertEquals(400, ex.getStatusCode().value());
    }

    @Test
    void getApplicationsByUserId_shouldReturnOwnApplications() {
        User currentUser = user(3L, User.Role.CANDIDATE);

        JobApplication app = new JobApplication();
        app.setId(1L);
        app.setUser(currentUser);
        app.setStatus("submitted");
        app.setApplicationDate(LocalDateTime.now());

        when(authContextService.requireCurrentUser()).thenReturn(currentUser);
        when(authContextService.isAdmin(currentUser)).thenReturn(false);
        when(jobApplicationRepository.findByUserId(3L)).thenReturn(List.of(app));

        assertEquals(1, jobApplicationService.getApplicationsByUserId(3L).size());
        verify(jobApplicationRepository).findByUserId(3L);
    }

    @Test
    void getApplicationsForMyJobs_shouldReturnRecruiterApplications() {
        User recruiter = user(7L, User.Role.RECRUITER);

        Company company = new Company();
        company.setId(1L);
        company.setCreatedBy(recruiter);

        Job job = new Job();
        job.setId(100L);
        job.setCompany(company);

        JobApplication app = new JobApplication();
        app.setId(999L);
        app.setJob(job);
        app.setUser(user(11L, User.Role.CANDIDATE));
        app.setStatus("submitted");
        app.setApplicationDate(LocalDateTime.now());

        when(authContextService.requireCurrentUser()).thenReturn(recruiter);
        when(authContextService.isAdmin(recruiter)).thenReturn(false);
        when(jobRepository.findManagedJobsByRecruiterId(eq(7L), anyList())).thenReturn(List.of(job));
        when(jobApplicationRepository.findByJobIdIn(List.of(100L))).thenReturn(List.of(app));

        assertEquals(1, jobApplicationService.getApplicationsForMyJobs().size());
        verify(jobApplicationRepository).findByJobIdIn(List.of(100L));
    }

    @Test
    void getApplicationsForMyJobs_shouldReturnForbidden_whenCandidateCalls() {
        User candidate = user(3L, User.Role.CANDIDATE);
        when(authContextService.requireCurrentUser()).thenReturn(candidate);
        when(authContextService.isAdmin(candidate)).thenReturn(false);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> jobApplicationService.getApplicationsForMyJobs());

        assertEquals(403, ex.getStatusCode().value());
        verify(jobRepository, never()).findManagedJobsByRecruiterId(eq(3L), anyList());
    }

    @Test
    void withdrawApplication_shouldReturnUpdatedApplication() {
        User currentUser = user(1L, User.Role.CANDIDATE);

        JobApplication app = new JobApplication();
        app.setId(88L);
        app.setUser(currentUser);
        app.setStatus("reviewing");
        app.setApplicationDate(LocalDateTime.now());

        when(authContextService.requireCurrentUser()).thenReturn(currentUser);
        when(jobApplicationRepository.findById(88L)).thenReturn(Optional.of(app));
        when(jobApplicationRepository.save(app)).thenReturn(app);

        JobApplicationDTO result = jobApplicationService.withdrawApplication(88L);
        assertEquals("withdrawn", result.getStatus());
    }

    @Test
    void getApplicationTimeline_shouldReturnAudits() {
        User currentUser = user(1L, User.Role.CANDIDATE);

        JobApplication app = new JobApplication();
        app.setId(5L);
        app.setUser(currentUser);

        JobApplicationStatusAudit audit = new JobApplicationStatusAudit();
        audit.setFromStatus("submitted");
        audit.setToStatus("reviewing");

        when(authContextService.requireCurrentUser()).thenReturn(currentUser);
        when(authContextService.isAdmin(currentUser)).thenReturn(false);
        when(jobApplicationRepository.findById(5L)).thenReturn(Optional.of(app));
        when(statusAuditRepository.findByApplicationIdOrderByChangedAtAsc(5L)).thenReturn(List.of(audit));

        assertEquals(1, jobApplicationService.getApplicationTimeline(5L).size());
    }

    private User user(Long id, User.Role role) {
        User user = new User();
        user.setId(id);
        user.setRole(role);
        user.setEmail("u" + id + "@mail.com");
        return user;
    }
}
