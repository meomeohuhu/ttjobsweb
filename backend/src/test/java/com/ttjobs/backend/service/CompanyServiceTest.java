package com.ttjobs.backend.service;

import com.ttjobs.backend.dto.company.CompanyMemberUpsertRequest;
import com.ttjobs.backend.dto.company.CompanyPublicPageDTO;
import com.ttjobs.backend.entity.Company;
import com.ttjobs.backend.entity.CompanyMember;
import com.ttjobs.backend.entity.Job;
import com.ttjobs.backend.entity.User;
import com.ttjobs.backend.repository.CompanyFollowRepository;
import com.ttjobs.backend.repository.CompanyMemberRepository;
import com.ttjobs.backend.repository.CompanyRepository;
import com.ttjobs.backend.repository.JobRepository;
import com.ttjobs.backend.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CompanyServiceTest {

    @Mock
    private CompanyRepository companyRepository;
    @Mock
    private CompanyMemberRepository companyMemberRepository;
    @Mock
    private AuthContextService authContextService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private CompanyAuthorizationService companyAuthorizationService;
    @Mock
    private JobRepository jobRepository;
    @Mock
    private CompanyFollowRepository companyFollowRepository;
    @Mock
    private CompanyVerificationStatusService companyVerificationStatusService;

    @InjectMocks
    private CompanyService companyService;

    @BeforeEach
    void setUpVerificationStatusFallback() {
        lenient().when(companyVerificationStatusService.getEffectiveStatus(any(Company.class)))
                .thenAnswer(invocation -> {
                    Company company = invocation.getArgument(0);
                    return company.getVerificationStatus() == null
                            ? Company.VerificationStatus.PENDING
                            : company.getVerificationStatus();
                });
        lenient().when(companyVerificationStatusService.isVerified(any(Company.class)))
                .thenAnswer(invocation -> {
                    Company company = invocation.getArgument(0);
                    return company.getVerificationStatus() == Company.VerificationStatus.VERIFIED;
                });
    }

    @Test
    void addCompanyMember_shouldReturnCreatedMember_whenValid() {
        User current = user(1L, User.Role.RECRUITER);
        User recruiter = user(2L, User.Role.RECRUITER);
        Company company = company(10L, current);

        CompanyMemberUpsertRequest req = new CompanyMemberUpsertRequest();
        req.setUserId(2L);
        req.setMemberRole("RECRUITER");

        CompanyMember saved = new CompanyMember();
        saved.setId(99L);
        saved.setCompany(company);
        saved.setUser(recruiter);
        saved.setMemberRole(CompanyMember.MemberRole.RECRUITER);

        when(authContextService.requireCurrentUser()).thenReturn(current);
        when(companyRepository.findByIdAndDeletedAtIsNull(10L)).thenReturn(Optional.of(company));
        doNothing().when(companyAuthorizationService).requireAdministerCompany(current, company);
        when(userRepository.findById(2L)).thenReturn(Optional.of(recruiter));
        when(companyMemberRepository.existsByCompanyIdAndUserId(10L, 2L)).thenReturn(false);
        when(companyMemberRepository.save(org.mockito.ArgumentMatchers.any(CompanyMember.class))).thenReturn(saved);

        assertEquals(99L, companyService.addCompanyMember(10L, req).getId());
    }

    @Test
    void addCompanyMember_shouldReturnConflict_whenDuplicate() {
        User current = user(1L, User.Role.RECRUITER);
        User recruiter = user(2L, User.Role.RECRUITER);
        Company company = company(10L, current);

        CompanyMemberUpsertRequest req = new CompanyMemberUpsertRequest();
        req.setUserId(2L);
        req.setMemberRole("RECRUITER");

        when(authContextService.requireCurrentUser()).thenReturn(current);
        when(companyRepository.findByIdAndDeletedAtIsNull(10L)).thenReturn(Optional.of(company));
        doNothing().when(companyAuthorizationService).requireAdministerCompany(current, company);
        when(userRepository.findById(2L)).thenReturn(Optional.of(recruiter));
        when(companyMemberRepository.existsByCompanyIdAndUserId(10L, 2L)).thenReturn(true);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> companyService.addCompanyMember(10L, req));
        assertEquals(409, ex.getStatusCode().value());
    }

    @Test
    void addCompanyMember_shouldReturnBadRequest_whenTargetIsCandidate() {
        User current = user(1L, User.Role.RECRUITER);
        User candidate = user(2L, User.Role.CANDIDATE);
        Company company = company(10L, current);

        CompanyMemberUpsertRequest req = new CompanyMemberUpsertRequest();
        req.setUserId(2L);
        req.setMemberRole("RECRUITER");

        when(authContextService.requireCurrentUser()).thenReturn(current);
        when(companyRepository.findByIdAndDeletedAtIsNull(10L)).thenReturn(Optional.of(company));
        doNothing().when(companyAuthorizationService).requireAdministerCompany(current, company);
        when(userRepository.findById(2L)).thenReturn(Optional.of(candidate));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> companyService.addCompanyMember(10L, req));
        assertEquals(400, ex.getStatusCode().value());
    }

    @Test
    void addCompanyMember_shouldReturnForbidden_whenRecruiterHasNoAdminPermission() {
        User current = user(1L, User.Role.RECRUITER);
        Company company = company(10L, user(9L, User.Role.RECRUITER));

        CompanyMemberUpsertRequest req = new CompanyMemberUpsertRequest();
        req.setUserId(2L);
        req.setMemberRole("RECRUITER");

        when(authContextService.requireCurrentUser()).thenReturn(current);
        when(companyRepository.findByIdAndDeletedAtIsNull(10L)).thenReturn(Optional.of(company));
        doThrow(new ResponseStatusException(org.springframework.http.HttpStatus.FORBIDDEN, "forbidden"))
                .when(companyAuthorizationService).requireAdministerCompany(current, company);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> companyService.addCompanyMember(10L, req));
        assertEquals(403, ex.getStatusCode().value());
    }

    @Test
    void removeCompanyMember_shouldReturnBadRequest_whenRemovingLastAdminMember() {
        User current = user(1L, User.Role.RECRUITER);
        Company company = company(10L, null);

        CompanyMember member = new CompanyMember();
        member.setId(44L);
        member.setCompany(company);
        member.setUser(user(2L, User.Role.RECRUITER));
        member.setMemberRole(CompanyMember.MemberRole.ADMIN);

        when(authContextService.requireCurrentUser()).thenReturn(current);
        when(companyRepository.findByIdAndDeletedAtIsNull(10L)).thenReturn(Optional.of(company));
        doNothing().when(companyAuthorizationService).requireAdministerCompany(current, company);
        when(companyMemberRepository.findByIdAndCompanyId(44L, 10L)).thenReturn(Optional.of(member));
        when(companyMemberRepository.countByCompanyIdAndMemberRole(10L, CompanyMember.MemberRole.ADMIN)).thenReturn(1L);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> companyService.removeCompanyMember(10L, 44L));
        assertEquals(400, ex.getStatusCode().value());
    }

    @Test
    void removeCompanyMember_shouldReturnBadRequest_whenTargetIsOwner() {
        User current = user(1L, User.Role.RECRUITER);
        User owner = user(10L, User.Role.RECRUITER);
        Company company = company(10L, owner);

        CompanyMember member = new CompanyMember();
        member.setId(44L);
        member.setCompany(company);
        member.setUser(owner);
        member.setMemberRole(CompanyMember.MemberRole.ADMIN);

        when(authContextService.requireCurrentUser()).thenReturn(current);
        when(companyRepository.findByIdAndDeletedAtIsNull(10L)).thenReturn(Optional.of(company));
        doNothing().when(companyAuthorizationService).requireAdministerCompany(current, company);
        when(companyMemberRepository.findByIdAndCompanyId(44L, 10L)).thenReturn(Optional.of(member));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> companyService.removeCompanyMember(10L, 44L));
        assertEquals(400, ex.getStatusCode().value());
    }

    @Test
    void getTopCompaniesBySavedJobs_shouldSortBySavedCount() {
        Company first = company(1L, null);
        first.setName("Alpha");
        first.setVerificationStatus(Company.VerificationStatus.VERIFIED);
        Company second = company(2L, null);
        second.setName("Beta");
        second.setVerificationStatus(Company.VerificationStatus.VERIFIED);

        when(companyRepository.findByDeletedAtIsNullAndVerificationStatus(Company.VerificationStatus.VERIFIED))
                .thenReturn(java.util.List.of(first, second));
        when(jobRepository.countByCompanyIdAndStatus(1L, "open")).thenReturn(5L);
        when(jobRepository.countByCompanyIdAndStatus(2L, "open")).thenReturn(10L);
        when(jobRepository.countSavedJobsByCompanyId(1L)).thenReturn(30L);
        when(jobRepository.countSavedJobsByCompanyId(2L)).thenReturn(12L);

        var result = companyService.getTopCompaniesBySavedJobs(2);

        assertEquals(2, result.size());
        assertEquals("Alpha", result.get(0).getName());
        assertEquals(30L, result.get(0).getSavedJobCount());
        assertEquals("Beta", result.get(1).getName());
    }

    @Test
    void getAllCompanies_shouldOnlyReturnVerifiedCompanies() {
        Company verified = company(1L, null);
        verified.setName("Verified");
        verified.setVerificationStatus(Company.VerificationStatus.VERIFIED);

        when(companyRepository.findByDeletedAtIsNullAndVerificationStatus(Company.VerificationStatus.VERIFIED))
                .thenReturn(java.util.List.of(verified));

        var result = companyService.getAllCompanies();

        assertEquals(1, result.size());
        assertEquals("Verified", result.get(0).getName());
    }

    @Test
    void getPublicCompanyJobs_shouldReturnNotFound_whenCompanyPending() {
        Company company = company(9L, null);
        company.setVerificationStatus(Company.VerificationStatus.PENDING);

        when(companyRepository.findByIdAndDeletedAtIsNull(9L)).thenReturn(Optional.of(company));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> companyService.getPublicCompanyJobs(9L));

        assertEquals(404, ex.getStatusCode().value());
    }

    @Test
    void getPublicCompanyPage_shouldReturnNotFound_whenCompanyPending() {
        Company company = company(9L, null);
        company.setVerificationStatus(Company.VerificationStatus.PENDING);

        when(companyRepository.findByIdAndDeletedAtIsNull(9L)).thenReturn(Optional.of(company));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> companyService.getPublicCompanyPage(9L));

        assertEquals(404, ex.getStatusCode().value());
    }

    @Test
    void getPublicCompanyJobs_shouldReturnOpenJobs() {
        Company company = company(9L, null);
        company.setName("Acme");
        company.setVerificationStatus(Company.VerificationStatus.VERIFIED);

        Job job = new Job();
        job.setId(101L);
        job.setTitle("Backend Engineer");
        job.setStatus("open");
        job.setCompany(company);

        when(companyRepository.findByIdAndDeletedAtIsNull(9L)).thenReturn(Optional.of(company));
        when(jobRepository.findCompanyJobsWithSavedCount(
                eq(9L),
                eq("open"),
                eq(Company.VerificationStatus.VERIFIED),
                org.mockito.ArgumentMatchers.any(Pageable.class)))
                .thenReturn(java.util.List.of(new com.ttjobs.backend.repository.JobWithSavedCount() {
                    @Override
                    public Job getJob() {
                        return job;
                    }

                    @Override
                    public Long getSavedCount() {
                        return 7L;
                    }
                }));

        var jobs = companyService.getPublicCompanyJobs(9L);

        assertEquals(1, jobs.size());
        assertEquals("Backend Engineer", jobs.get(0).getTitle());
        assertEquals(7L, jobs.get(0).getSavedCount());
        assertTrue(jobs.get(0).getCompanyName().contains("Acme"));
    }

    @Test
    void getPublicCompanyPage_shouldReturnCompanyAndJobs() {
        Company company = company(9L, null);
        company.setName("Acme");
        company.setLogoUrl("https://cdn.example.com/logo.png");
        company.setVerificationStatus(Company.VerificationStatus.VERIFIED);

        Job job = new Job();
        job.setId(101L);
        job.setTitle("Backend Engineer");
        job.setStatus("open");
        job.setCompany(company);

        when(companyRepository.findByIdAndDeletedAtIsNull(9L)).thenReturn(Optional.of(company));
        when(jobRepository.findCompanyJobsWithSavedCount(
                eq(9L),
                eq("open"),
                eq(Company.VerificationStatus.VERIFIED),
                org.mockito.ArgumentMatchers.any(Pageable.class)))
                .thenReturn(java.util.List.of(new com.ttjobs.backend.repository.JobWithSavedCount() {
                    @Override
                    public Job getJob() {
                        return job;
                    }

                    @Override
                    public Long getSavedCount() {
                        return 3L;
                    }
                }));

        CompanyPublicPageDTO payload = companyService.getPublicCompanyPage(9L);

        assertEquals("Acme", payload.getCompany().getName());
        assertEquals("https://cdn.example.com/logo.png", payload.getCompany().getLogoUrl());
        assertEquals(1, payload.getJobs().size());
        assertEquals("Backend Engineer", payload.getJobs().get(0).getTitle());
    }

    private User user(Long id, User.Role role) {
        User user = new User();
        user.setId(id);
        user.setRole(role);
        user.setEmail("u" + id + "@mail.com");
        return user;
    }

    private Company company(Long id, User owner) {
        Company company = new Company();
        company.setId(id);
        company.setCreatedBy(owner);
        return company;
    }
}

