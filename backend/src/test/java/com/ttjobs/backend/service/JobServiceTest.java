package com.ttjobs.backend.service;

import com.ttjobs.backend.entity.Company;
import com.ttjobs.backend.entity.Job;
import com.ttjobs.backend.entity.User;
import com.ttjobs.backend.repository.CompanyMemberRepository;
import com.ttjobs.backend.repository.CompanyRepository;
import com.ttjobs.backend.repository.JobRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JobServiceTest {

    @Mock
    private JobRepository jobRepository;
    @Mock
    private CompanyRepository companyRepository;
    @Mock
    private CompanyMemberRepository companyMemberRepository;
    @Mock
    private CompanyAuthorizationService companyAuthorizationService;
    @Mock
    private AuthContextService authContextService;

    @InjectMocks
    private JobService jobService;

    @Test
    void createJob_shouldReturnForbidden_whenUserIsCandidate() {
        User candidate = user(1L, User.Role.CANDIDATE);
        Job job = new Job();
        Company company = new Company();
        company.setId(2L);
        job.setCompany(company);

        when(authContextService.requireCurrentUser()).thenReturn(candidate);
        when(authContextService.isAdmin(candidate)).thenReturn(false);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> jobService.createJob(job));

        assertEquals(403, ex.getStatusCode().value());
    }

    @Test
    void updateJob_shouldReturnBadRequest_whenStatusTransitionInvalid() {
        User recruiter = user(1L, User.Role.RECRUITER);
        Company company = new Company();
        company.setCreatedBy(recruiter);

        Job existing = new Job();
        existing.setId(10L);
        existing.setCompany(company);
        existing.setStatus("open");

        Job update = new Job();
        update.setStatus("draft");

        when(authContextService.requireCurrentUser()).thenReturn(recruiter);
        when(authContextService.isAdmin(recruiter)).thenReturn(false);
        when(jobRepository.findByIdAndDeletedAtIsNull(10L)).thenReturn(Optional.of(existing));
        doNothing().when(companyAuthorizationService).requireManageCompany(recruiter, company);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> jobService.updateJob(10L, update));

        assertEquals(400, ex.getStatusCode().value());
    }

    @Test
    void createJob_shouldReturnForbidden_whenRecruiterDoesNotOwnCompany() {
        User recruiter = user(1L, User.Role.RECRUITER);
        User owner = user(2L, User.Role.RECRUITER);

        Company company = new Company();
        company.setId(11L);
        company.setCreatedBy(owner);

        Job job = new Job();
        job.setCompany(company);
        job.setTitle("Java Dev");

        when(authContextService.requireCurrentUser()).thenReturn(recruiter);
        when(authContextService.isAdmin(recruiter)).thenReturn(false);
        when(companyRepository.findByIdAndDeletedAtIsNull(11L)).thenReturn(Optional.of(company));
        doThrow(new ResponseStatusException(org.springframework.http.HttpStatus.FORBIDDEN, "forbidden"))
                .when(companyAuthorizationService).requireManageCompany(recruiter, company);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> jobService.createJob(job));

        assertEquals(403, ex.getStatusCode().value());
    }

    @Test
    void getJobById_shouldReturnForbidden_whenJobClosedAndUserNotOwner() {
        User recruiter = user(5L, User.Role.RECRUITER);
        User owner = user(1L, User.Role.RECRUITER);

        Company company = new Company();
        company.setCreatedBy(owner);

        Job job = new Job();
        job.setId(99L);
        job.setCompany(company);
        job.setStatus("closed");

        when(authContextService.requireCurrentUser()).thenReturn(recruiter);
        when(jobRepository.findByIdAndDeletedAtIsNull(99L)).thenReturn(Optional.of(job));
        when(companyAuthorizationService.canManageCompany(recruiter, company)).thenReturn(false);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> jobService.getJobById(99L));

        assertEquals(403, ex.getStatusCode().value());
    }

    @Test
    void getCompanyJobs_shouldReturnForbidden_whenRecruiterNotOwner() {
        User recruiter = user(9L, User.Role.RECRUITER);
        User owner = user(1L, User.Role.RECRUITER);

        Company company = new Company();
        company.setId(3L);
        company.setCreatedBy(owner);

        when(authContextService.requireCurrentUser()).thenReturn(recruiter);
        when(authContextService.isAdmin(recruiter)).thenReturn(false);
        when(companyRepository.findByIdAndDeletedAtIsNull(3L)).thenReturn(Optional.of(company));
        doThrow(new ResponseStatusException(org.springframework.http.HttpStatus.FORBIDDEN, "forbidden"))
                .when(companyAuthorizationService).requireManageCompany(recruiter, company);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> jobService.getCompanyJobs(3L));

        assertEquals(403, ex.getStatusCode().value());
    }

    @Test
    void getCompanyJobs_shouldReturnJobs_whenOwnerCalls() {
        User recruiter = user(1L, User.Role.RECRUITER);

        Company company = new Company();
        company.setId(5L);
        company.setCreatedBy(recruiter);

        Job job = new Job();
        job.setId(101L);
        job.setTitle("Backend");
        job.setStatus("open");
        job.setCompany(company);

        when(authContextService.requireCurrentUser()).thenReturn(recruiter);
        when(authContextService.isAdmin(recruiter)).thenReturn(false);
        when(companyRepository.findByIdAndDeletedAtIsNull(5L)).thenReturn(Optional.of(company));
        when(jobRepository.findByCompanyIdAndDeletedAtIsNull(5L)).thenReturn(List.of(job));
        doNothing().when(companyAuthorizationService).requireManageCompany(recruiter, company);

        assertEquals(1, jobService.getCompanyJobs(5L).size());
    }

    private User user(Long id, User.Role role) {
        User user = new User();
        user.setId(id);
        user.setRole(role);
        user.setEmail("u" + id + "@mail.com");
        return user;
    }
}
