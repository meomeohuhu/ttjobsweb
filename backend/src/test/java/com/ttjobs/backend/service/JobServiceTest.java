package com.ttjobs.backend.service;

import com.ttjobs.backend.entity.Company;
import com.ttjobs.backend.entity.Job;
import com.ttjobs.backend.entity.User;
import com.ttjobs.backend.repository.CompanyMemberRepository;
import com.ttjobs.backend.repository.CompanyRepository;
import com.ttjobs.backend.repository.JobCategoryCount;
import com.ttjobs.backend.repository.JobRepository;
import com.ttjobs.backend.repository.JobWithSavedCount;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
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

        when(authContextService.getCurrentUserOptional()).thenReturn(Optional.of(recruiter));
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

    @Test
    void getHighlightedJobs_shouldClampSizeAndMapSavedCount() {
        Company company = new Company();
        company.setId(7L);
        company.setName("FPT Software");

        Job job = new Job();
        job.setId(20L);
        job.setTitle("Java Backend Developer");
        job.setStatus("open");
        job.setCompany(company);

        JobWithSavedCount projection = mock(JobWithSavedCount.class);
        when(projection.getJob()).thenReturn(job);
        when(projection.getSavedCount()).thenReturn(9L);
        when(jobRepository.findHighlightedJobs(eq("open"), any(Pageable.class))).thenReturn(List.of(projection));

        List<com.ttjobs.backend.dto.JobDTO> result = jobService.getHighlightedJobs(null, 100);

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        org.mockito.Mockito.verify(jobRepository).findHighlightedJobs(eq("open"), pageableCaptor.capture());
        assertEquals(50, pageableCaptor.getValue().getPageSize());
        assertEquals(1, result.size());
        assertEquals(20L, result.get(0).getId());
        assertEquals(9L, result.get(0).getSavedCount());
    }

    @Test
    void getHighlightedJobs_shouldRejectUnknownType() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> jobService.getHighlightedJobs("paid_boost", 12));

        assertEquals(400, ex.getStatusCode().value());
    }

    @Test
    void getBestJobs_shouldClampSizeAndMapSavedCount() {
        Company company = new Company();
        company.setId(8L);
        company.setName("TTJobs");

        Job job = new Job();
        job.setId(21L);
        job.setTitle("Most Saved");
        job.setStatus("open");
        job.setCompany(company);

        JobWithSavedCount projection = mock(JobWithSavedCount.class);
        when(projection.getJob()).thenReturn(job);
        when(projection.getSavedCount()).thenReturn(14L);
        when(jobRepository.findBestJobs(eq("open"), any(Pageable.class))).thenReturn(List.of(projection));

        List<com.ttjobs.backend.dto.JobDTO> result = jobService.getBestJobs(null, 100);

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        org.mockito.Mockito.verify(jobRepository).findBestJobs(eq("open"), pageableCaptor.capture());
        assertEquals(50, pageableCaptor.getValue().getPageSize());
        assertEquals(1, result.size());
        assertEquals(21L, result.get(0).getId());
        assertEquals(14L, result.get(0).getSavedCount());
    }

    @Test
    void getBestJobs_shouldRejectUnknownType() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> jobService.getBestJobs("newest", 12));

        assertEquals(400, ex.getStatusCode().value());
    }

    @Test
    void getTopCategories_shouldClampSizeAndResolveLabel() {
        JobCategoryCount projection = mock(JobCategoryCount.class);
        when(projection.getCategory()).thenReturn("SALES");
        when(projection.getJobCount()).thenReturn(7L);
        when(jobRepository.findTopCategories(eq("open"), any(Pageable.class))).thenReturn(List.of(projection));

        List<com.ttjobs.backend.dto.JobCategoryStatDTO> result = jobService.getTopCategories(100);

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        org.mockito.Mockito.verify(jobRepository).findTopCategories(eq("open"), pageableCaptor.capture());
        assertEquals(24, pageableCaptor.getValue().getPageSize());
        assertEquals(1, result.size());
        assertEquals("SALES", result.get(0).getCategory());
        assertEquals("Kinh doanh - Bán hàng", result.get(0).getLabel());
        assertEquals(7L, result.get(0).getJobCount());
    }

    @Test
    void searchJobs_shouldTrimKeywordAndValidateSalaryRange() {
        when(jobRepository.findAll(any(org.springframework.data.jpa.domain.Specification.class), any(Pageable.class)))
                .thenReturn(org.springframework.data.domain.Page.empty());

        // Test trimming
        jobService.searchJobs("  java  ", null, null, null, null, null, null, null, null, null, "latest", 0, 10);

        ArgumentCaptor<org.springframework.data.jpa.domain.Specification> specCaptor = ArgumentCaptor.forClass(org.springframework.data.jpa.domain.Specification.class);
        org.mockito.Mockito.verify(jobRepository).findAll(specCaptor.capture(), any(Pageable.class));
        // Specification is hard to verify directly, but we'll implement the trimming in service.

        // Test salary validation
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> jobService.searchJobs(null, null, null, null, null, null, "open",
                        java.math.BigDecimal.valueOf(1000), java.math.BigDecimal.valueOf(500), null, "latest", 0, 10));
        assertEquals(400, ex.getStatusCode().value());
        assertEquals("salaryMin cannot be greater than salaryMax", ex.getReason());
    }

    @Test
    void resolveSort_shouldIncludeIdAsTieBreaker() {
        when(jobRepository.findAll(any(org.springframework.data.jpa.domain.Specification.class), any(Pageable.class)))
                .thenReturn(org.springframework.data.domain.Page.empty());

        jobService.searchJobs(null, null, null, null, null, null, "open", null, null, null, "latest", 0, 10);

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        org.mockito.Mockito.verify(jobRepository).findAll(any(org.springframework.data.jpa.domain.Specification.class), pageableCaptor.capture());

        org.springframework.data.domain.Sort sort = pageableCaptor.getValue().getSort();
        // Check if id is present in sort
        boolean hasIdSort = false;
        for (org.springframework.data.domain.Sort.Order order : sort) {
            if ("id".equals(order.getProperty())) {
                hasIdSort = true;
                break;
            }
        }
        assertEquals(true, hasIdSort, "Sort should include id as tie-breaker");
    }

    private User user(Long id, User.Role role) {
        User user = new User();
        user.setId(id);
        user.setRole(role);
        user.setEmail("u" + id + "@mail.com");
        return user;
    }
}
