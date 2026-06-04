package com.ttjobs.backend.service;

import com.ttjobs.backend.dto.recruiter.RecruitmentCampaignRequest;
import com.ttjobs.backend.entity.Company;
import com.ttjobs.backend.entity.CompanyMember;
import com.ttjobs.backend.entity.Job;
import com.ttjobs.backend.entity.RecruitmentCampaign;
import com.ttjobs.backend.entity.User;
import com.ttjobs.backend.repository.CompanyRepository;
import com.ttjobs.backend.repository.JobRepository;
import com.ttjobs.backend.repository.RecruitmentCampaignRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RecruiterWorkspaceServiceTest {

    @Mock
    private AuthContextService authContextService;
    @Mock
    private CompanyAuthorizationService companyAuthorizationService;
    @Mock
    private CompanyRepository companyRepository;
    @Mock
    private JobRepository jobRepository;
    @Mock
    private RecruitmentCampaignRepository campaignRepository;

    @InjectMocks
    private RecruiterWorkspaceService service;

    @Test
    void saveCampaign_shouldCreateCampaignWithManagedJobs() {
        User recruiter = user(1L, User.Role.RECRUITER);
        Company company = company(10L, recruiter);
        Job job = job(20L, company);

        when(authContextService.requireCurrentUser()).thenReturn(recruiter);
        when(authContextService.isAdmin(recruiter)).thenReturn(false);
        when(companyRepository.findByIdAndDeletedAtIsNull(10L)).thenReturn(Optional.of(company));
        when(companyAuthorizationService.canManageCompany(recruiter, company)).thenReturn(true);
        when(jobRepository.findManagedJobsByRecruiterId(
                recruiter.getId(),
                List.of(CompanyMember.MemberRole.RECRUITER, CompanyMember.MemberRole.ADMIN)))
                .thenReturn(List.of(job));
        when(campaignRepository.save(any(RecruitmentCampaign.class))).thenAnswer(invocation -> {
            RecruitmentCampaign campaign = invocation.getArgument(0);
            campaign.setId(99L);
            return campaign;
        });

        var dto = service.saveCampaign(null, campaignRequest(10L, "TEST_QA Campaign", "active", List.of(20L)));

        assertEquals(99L, dto.getId());
        assertEquals("active", dto.getStatus());
        assertEquals(1L, dto.getJobCount());
        assertEquals(List.of(20L), dto.getJobIds());
    }

    @Test
    void saveCampaign_shouldUpdateManagedCollectionWithoutReplacingDetachedList() {
        User recruiter = user(1L, User.Role.RECRUITER);
        Company company = company(10L, recruiter);
        Job oldJob = job(20L, company);
        Job newJob = job(21L, company);
        RecruitmentCampaign existing = new RecruitmentCampaign();
        existing.setId(99L);
        existing.setCompany(company);
        existing.setCreatedBy(recruiter);
        existing.setName("Old");
        existing.setStatus("active");
        existing.setJobs(new ArrayList<>(List.of(oldJob)));

        when(authContextService.requireCurrentUser()).thenReturn(recruiter);
        when(authContextService.isAdmin(recruiter)).thenReturn(false);
        when(companyRepository.findByIdAndDeletedAtIsNull(10L)).thenReturn(Optional.of(company));
        when(companyAuthorizationService.canManageCompany(recruiter, company)).thenReturn(true);
        when(campaignRepository.findById(99L)).thenReturn(Optional.of(existing));
        when(jobRepository.findManagedJobsByRecruiterId(
                recruiter.getId(),
                List.of(CompanyMember.MemberRole.RECRUITER, CompanyMember.MemberRole.ADMIN)))
                .thenReturn(List.of(oldJob, newJob));
        when(campaignRepository.save(existing)).thenReturn(existing);

        var dto = service.saveCampaign(99L, campaignRequest(10L, "Updated", "paused", List.of(21L)));

        assertEquals("Updated", existing.getName());
        assertEquals("paused", dto.getStatus());
        assertEquals(1L, dto.getJobCount());
        assertEquals(List.of(21L), dto.getJobIds());
        assertTrue(existing.getJobs().contains(newJob));
    }

    @Test
    void saveCampaign_shouldReturnForbidden_whenExistingCampaignCompanyNotManaged() {
        User recruiter = user(1L, User.Role.RECRUITER);
        Company requestedCompany = company(10L, recruiter);
        Company otherCompany = company(11L, user(2L, User.Role.RECRUITER));
        RecruitmentCampaign existing = new RecruitmentCampaign();
        existing.setId(99L);
        existing.setCompany(otherCompany);

        when(authContextService.requireCurrentUser()).thenReturn(recruiter);
        when(authContextService.isAdmin(recruiter)).thenReturn(false);
        when(companyRepository.findByIdAndDeletedAtIsNull(10L)).thenReturn(Optional.of(requestedCompany));
        when(companyAuthorizationService.canManageCompany(recruiter, requestedCompany)).thenReturn(true);
        when(campaignRepository.findById(99L)).thenReturn(Optional.of(existing));
        when(companyAuthorizationService.canManageCompany(recruiter, otherCompany)).thenReturn(false);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.saveCampaign(99L, campaignRequest(10L, "Updated", "paused", List.of())));

        assertEquals(403, ex.getStatusCode().value());
    }

    private RecruitmentCampaignRequest campaignRequest(Long companyId, String name, String status, List<Long> jobIds) {
        RecruitmentCampaignRequest request = new RecruitmentCampaignRequest();
        request.setCompanyId(companyId);
        request.setName(name);
        request.setDescription("QA campaign");
        request.setStatus(status);
        request.setTargetHires(2);
        request.setStartsAt(LocalDateTime.now());
        request.setEndsAt(LocalDateTime.now().plusDays(7));
        request.setJobIds(jobIds);
        return request;
    }

    private Job job(Long id, Company company) {
        Job job = new Job();
        job.setId(id);
        job.setTitle("Job " + id);
        job.setCompany(company);
        return job;
    }

    private Company company(Long id, User owner) {
        Company company = new Company();
        company.setId(id);
        company.setName("Company " + id);
        company.setCreatedBy(owner);
        return company;
    }

    private User user(Long id, User.Role role) {
        User user = new User();
        user.setId(id);
        user.setEmail("u" + id + "@test.local");
        user.setRole(role);
        return user;
    }
}
