package com.ttjobs.backend.service;

import com.ttjobs.backend.dto.ApplicationTimelineDTO;
import com.ttjobs.backend.dto.RecruiterApplicationDTO;
import com.ttjobs.backend.dto.RecruiterApplicationDetailDTO;
import com.ttjobs.backend.dto.RecruiterCompanyDTO;
import com.ttjobs.backend.dto.RecruiterJobDTO;
import com.ttjobs.backend.entity.Company;
import com.ttjobs.backend.entity.CompanyMember;
import com.ttjobs.backend.entity.Job;
import com.ttjobs.backend.entity.JobApplication;
import com.ttjobs.backend.entity.JobApplicationStatusAudit;
import com.ttjobs.backend.entity.User;
import com.ttjobs.backend.repository.CompanyFollowRepository;
import com.ttjobs.backend.repository.CompanyMemberRepository;
import com.ttjobs.backend.repository.CompanyRepository;
import com.ttjobs.backend.repository.JobApplicationRepository;
import com.ttjobs.backend.repository.JobApplicationStatusAuditRepository;
import com.ttjobs.backend.repository.JobRepository;
import com.ttjobs.backend.repository.SavedJobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class RecruiterWorkspaceService {

    private static final Set<String> MANAGED_MEMBER_ROLES = Set.of("ADMIN", "RECRUITER");

    @Autowired
    private AuthContextService authContextService;
    @Autowired
    private CompanyAuthorizationService companyAuthorizationService;
    @Autowired
    private CompanyRepository companyRepository;
    @Autowired
    private CompanyMemberRepository companyMemberRepository;
    @Autowired
    private CompanyFollowRepository companyFollowRepository;
    @Autowired
    private JobRepository jobRepository;
    @Autowired
    private JobApplicationRepository jobApplicationRepository;
    @Autowired
    private JobApplicationStatusAuditRepository statusAuditRepository;
    @Autowired
    private SavedJobRepository savedJobRepository;

    public List<RecruiterCompanyDTO> getManagedCompanies() {
        User currentUser = requireRecruiterOrAdmin();
        List<Job> managedJobs = loadManagedJobs(currentUser);
        Map<Long, List<Job>> jobsByCompany = managedJobs.stream()
                .filter(job -> job.getCompany() != null && job.getCompany().getId() != null)
                .collect(Collectors.groupingBy(job -> job.getCompany().getId()));

        return loadManagedCompanies(currentUser).stream()
                .sorted(Comparator.comparing(company -> safeLower(company.getName())))
                .map(company -> toCompanyDto(company, currentUser, jobsByCompany.getOrDefault(company.getId(), List.of())))
                .toList();
    }

    public List<RecruiterJobDTO> getManagedJobs(Long companyId, String status, String keyword, Integer page, Integer size) {
        User currentUser = requireRecruiterOrAdmin();
        List<JobApplication> applications = loadManagedApplications(currentUser);
        Map<Long, Long> applicationCounts = applications.stream()
                .filter(application -> application.getJob() != null)
                .collect(Collectors.groupingBy(application -> application.getJob().getId(), Collectors.counting()));
        LocalDateTime newThreshold = LocalDateTime.now().minusDays(7);
        Map<Long, Long> newApplicationCounts = applications.stream()
                .filter(application -> application.getJob() != null)
                .filter(application -> application.getApplicationDate() != null)
                .filter(application -> !application.getApplicationDate().isBefore(newThreshold))
                .collect(Collectors.groupingBy(application -> application.getJob().getId(), Collectors.counting()));

        return paginate(loadManagedJobs(currentUser).stream()
                .filter(job -> companyId == null || (job.getCompany() != null && companyId.equals(job.getCompany().getId())))
                .filter(job -> isBlank(status) || status.equalsIgnoreCase(job.getStatus()))
                .filter(job -> matchesJobKeyword(job, keyword))
                .sorted(Comparator.comparing(Job::getPostedDate, Comparator.nullsLast(Comparator.reverseOrder())))
                .map(job -> toJobDto(job, applicationCounts, newApplicationCounts))
                .toList(), page, size);
    }

    public List<RecruiterApplicationDTO> getManagedApplications(Long companyId, Long jobId, String status,
                                                               String keyword, Integer page, Integer size) {
        User currentUser = requireRecruiterOrAdmin();
        return paginate(loadManagedApplications(currentUser).stream()
                .filter(application -> companyId == null || companyId.equals(applicationCompanyId(application)))
                .filter(application -> jobId == null || jobId.equals(applicationJobId(application)))
                .filter(application -> isBlank(status) || status.equalsIgnoreCase(application.getStatus()))
                .filter(application -> matchesApplicationKeyword(application, keyword))
                .sorted(Comparator.comparing(JobApplication::getApplicationDate, Comparator.nullsLast(Comparator.reverseOrder())))
                .map(this::toApplicationDto)
                .toList(), page, size);
    }

    public RecruiterApplicationDetailDTO getManagedApplicationDetail(Long applicationId) {
        User currentUser = requireRecruiterOrAdmin();
        JobApplication application = jobApplicationRepository.findByIdWithDetails(applicationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Application not found"));

        requireApplicationAccess(currentUser, application);
        return toApplicationDetailDto(application);
    }

    private User requireRecruiterOrAdmin() {
        User currentUser = authContextService.requireCurrentUser();
        if (currentUser.getRole() != User.Role.RECRUITER && !authContextService.isAdmin(currentUser)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only recruiter can access recruiter workspace");
        }
        return currentUser;
    }

    private List<Company> loadManagedCompanies(User currentUser) {
        return companyRepository.findByDeletedAtIsNull().stream()
                .filter(company -> authContextService.isAdmin(currentUser)
                        || companyAuthorizationService.canManageCompany(currentUser, company))
                .toList();
    }

    private List<Job> loadManagedJobs(User currentUser) {
        if (authContextService.isAdmin(currentUser)) {
            return jobRepository.findAll().stream()
                    .filter(job -> job.getDeletedAt() == null)
                    .filter(job -> job.getCompany() != null && job.getCompany().getDeletedAt() == null)
                    .toList();
        }
        return jobRepository.findManagedJobsByRecruiterId(
                        currentUser.getId(),
                        List.of(CompanyMember.MemberRole.RECRUITER, CompanyMember.MemberRole.ADMIN)
                ).stream()
                .filter(job -> job.getDeletedAt() == null)
                .filter(job -> job.getCompany() != null && job.getCompany().getDeletedAt() == null)
                .toList();
    }

    private List<JobApplication> loadManagedApplications(User currentUser) {
        if (authContextService.isAdmin(currentUser)) {
            return jobApplicationRepository.findAll();
        }
        List<Long> jobIds = loadManagedJobs(currentUser).stream()
                .map(Job::getId)
                .filter(Objects::nonNull)
                .toList();
        if (jobIds.isEmpty()) {
            return List.of();
        }
        return jobApplicationRepository.findByJobIdIn(jobIds);
    }

    private void requireApplicationAccess(User currentUser, JobApplication application) {
        if (authContextService.isAdmin(currentUser)) {
            return;
        }
        if (application.getJob() == null || application.getJob().getCompany() == null
                || !companyAuthorizationService.canManageCompany(currentUser, application.getJob().getCompany())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You cannot access this application");
        }
    }

    private RecruiterCompanyDTO toCompanyDto(Company company, User currentUser, List<Job> jobs) {
        RecruiterCompanyDTO dto = new RecruiterCompanyDTO();
        dto.setId(company.getId());
        dto.setName(company.getName());
        dto.setDescription(company.getDescription());
        dto.setLocation(company.getLocation());
        dto.setWebsite(company.getWebsite());
        dto.setIndustry(company.getIndustry());
        dto.setLogoUrl(company.getLogoUrl());
        dto.setMemberRole(resolveMemberRole(company, currentUser));
        dto.setJobCount((long) jobs.size());
        dto.setOpenJobCount(jobs.stream().filter(job -> "open".equalsIgnoreCase(job.getStatus())).count());
        dto.setMemberCount((long) companyMemberRepository.findByCompanyId(company.getId()).size());
        dto.setFollowerCount(companyFollowRepository.countByCompanyId(company.getId()));
        return dto;
    }

    private String resolveMemberRole(Company company, User currentUser) {
        if (authContextService.isAdmin(currentUser)) {
            return "ADMIN";
        }
        if (company.getCreatedBy() != null && currentUser.getId().equals(company.getCreatedBy().getId())) {
            return "ADMIN";
        }
        return companyMemberRepository.findByCompanyId(company.getId()).stream()
                .filter(member -> member.getUser() != null && currentUser.getId().equals(member.getUser().getId()))
                .map(member -> member.getMemberRole() == null ? null : member.getMemberRole().name())
                .filter(role -> role != null && MANAGED_MEMBER_ROLES.contains(role))
                .findFirst()
                .orElse("RECRUITER");
    }

    private RecruiterJobDTO toJobDto(Job job, Map<Long, Long> applicationCounts, Map<Long, Long> newApplicationCounts) {
        RecruiterJobDTO dto = new RecruiterJobDTO();
        dto.setId(job.getId());
        dto.setTitle(job.getTitle());
        dto.setDescription(job.getDescription());
        dto.setLocation(job.getLocation());
        dto.setSalary(job.getSalary());
        dto.setSalaryMin(job.getSalaryMin());
        dto.setSalaryMax(job.getSalaryMax());
        dto.setCurrency(job.getCurrency());
        dto.setJobType(job.getJobType());
        dto.setExperienceLevel(job.getExperienceLevel());
        dto.setCategory(job.getCategory());
        dto.setStatus(job.getStatus());
        dto.setPostedDate(job.getPostedDate());
        dto.setApplicationDeadline(job.getApplicationDeadline());
        if (job.getCompany() != null) {
            dto.setCompanyId(job.getCompany().getId());
            dto.setCompanyName(job.getCompany().getName());
            dto.setCompanyLogoUrl(job.getCompany().getLogoUrl());
        }
        dto.setSavedCount(job.getId() == null ? 0L : savedJobRepository.countByJobId(job.getId()));
        dto.setApplicationCount(applicationCounts.getOrDefault(job.getId(), 0L));
        dto.setNewApplicationCount(newApplicationCounts.getOrDefault(job.getId(), 0L));
        return dto;
    }

    private RecruiterApplicationDTO toApplicationDto(JobApplication application) {
        RecruiterApplicationDTO dto = new RecruiterApplicationDTO();
        dto.setId(application.getId());
        dto.setApplicationDate(application.getApplicationDate());
        dto.setStatus(application.getStatus());
        if (application.getUser() != null) {
            dto.setCandidateId(application.getUser().getId());
            dto.setCandidateName(application.getUser().getName());
            dto.setCandidateEmail(application.getUser().getEmail());
            dto.setCandidatePhone(application.getUser().getPhone());
        }
        if (application.getJob() != null) {
            dto.setJobId(application.getJob().getId());
            dto.setJobTitle(application.getJob().getTitle());
            if (application.getJob().getCompany() != null) {
                dto.setCompanyId(application.getJob().getCompany().getId());
                dto.setCompanyName(application.getJob().getCompany().getName());
            }
        }
        dto.setHasCv(hasCv(application));
        return dto;
    }

    private RecruiterApplicationDetailDTO toApplicationDetailDto(JobApplication application) {
        RecruiterApplicationDetailDTO dto = new RecruiterApplicationDetailDTO();
        dto.setId(application.getId());
        dto.setApplicationDate(application.getApplicationDate());
        dto.setStatus(application.getStatus());
        if (application.getUser() != null) {
            dto.setCandidateId(application.getUser().getId());
            dto.setCandidateName(application.getUser().getName());
            dto.setCandidateEmail(application.getUser().getEmail());
            dto.setCandidatePhone(application.getUser().getPhone());
            dto.setCandidateAddress(application.getUser().getAddress());
            dto.setCandidateExperienceYears(application.getUser().getExperienceYears());
        }
        if (application.getJob() != null) {
            dto.setJobId(application.getJob().getId());
            dto.setJobTitle(application.getJob().getTitle());
            dto.setJobStatus(application.getJob().getStatus());
            if (application.getJob().getCompany() != null) {
                dto.setCompanyId(application.getJob().getCompany().getId());
                dto.setCompanyName(application.getJob().getCompany().getName());
            }
        }
        dto.setHasCv(hasCv(application));
        dto.setTimeline(statusAuditRepository.findByApplicationIdOrderByChangedAtAsc(application.getId())
                .stream()
                .map(this::toTimelineDto)
                .toList());
        return dto;
    }

    private ApplicationTimelineDTO toTimelineDto(JobApplicationStatusAudit audit) {
        ApplicationTimelineDTO dto = new ApplicationTimelineDTO();
        dto.setFromStatus(audit.getFromStatus());
        dto.setToStatus(audit.getToStatus());
        dto.setChangedAt(audit.getChangedAt());
        return dto;
    }

    private boolean matchesJobKeyword(Job job, String keyword) {
        if (isBlank(keyword)) {
            return true;
        }
        String value = keyword.toLowerCase(Locale.ROOT);
        return contains(job.getTitle(), value)
                || contains(job.getDescription(), value)
                || contains(job.getLocation(), value)
                || (job.getCompany() != null && contains(job.getCompany().getName(), value));
    }

    private boolean matchesApplicationKeyword(JobApplication application, String keyword) {
        if (isBlank(keyword)) {
            return true;
        }
        String value = keyword.toLowerCase(Locale.ROOT);
        return (application.getUser() != null && (contains(application.getUser().getName(), value)
                || contains(application.getUser().getEmail(), value)))
                || (application.getJob() != null && contains(application.getJob().getTitle(), value))
                || (application.getJob() != null && application.getJob().getCompany() != null
                && contains(application.getJob().getCompany().getName(), value));
    }

    private Long applicationCompanyId(JobApplication application) {
        return application.getJob() != null && application.getJob().getCompany() != null
                ? application.getJob().getCompany().getId()
                : null;
    }

    private Long applicationJobId(JobApplication application) {
        return application.getJob() != null ? application.getJob().getId() : null;
    }

    private boolean hasCv(JobApplication application) {
        return application.getCvUrl() != null && !application.getCvUrl().isBlank();
    }

    private boolean contains(String text, String normalizedNeedle) {
        return text != null && text.toLowerCase(Locale.ROOT).contains(normalizedNeedle);
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private String safeLower(String value) {
        return value == null ? "" : value.toLowerCase(Locale.ROOT);
    }

    private <T> List<T> paginate(List<T> items, Integer page, Integer size) {
        int safePage = page == null ? 0 : Math.max(page, 0);
        int safeSize = size == null ? 50 : Math.max(1, Math.min(size, 100));
        int from = Math.min(safePage * safeSize, items.size());
        int to = Math.min(from + safeSize, items.size());
        return items.subList(from, to);
    }
}
