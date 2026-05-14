package com.ttjobs.backend.service;

import com.ttjobs.backend.dto.admin.AdminRoleUpdateRequest;
import com.ttjobs.backend.dto.admin.AdminCompanyUpdateRequest;
import com.ttjobs.backend.dto.admin.AdminJobUpdateRequest;
import com.ttjobs.backend.dto.admin.AdminPeriodMetricsDTO;
import com.ttjobs.backend.dto.admin.AdminStatsDTO;
import com.ttjobs.backend.dto.admin.AdminUserDTO;
import com.ttjobs.backend.dto.common.AdminActionRequest;
import com.ttjobs.backend.dto.company.CompanyDTO;
import com.ttjobs.backend.dto.job.JobDTO;
import com.ttjobs.backend.entity.Company;
import com.ttjobs.backend.entity.CompanyVerification;
import com.ttjobs.backend.entity.InterviewSchedule;
import com.ttjobs.backend.entity.JobApplication;
import com.ttjobs.backend.entity.Job;
import com.ttjobs.backend.entity.User;
import com.ttjobs.backend.repository.CandidateJobMatchRepository;
import com.ttjobs.backend.repository.CompanyFollowRepository;
import com.ttjobs.backend.repository.CompanyRepository;
import com.ttjobs.backend.repository.CompanyVerificationRepository;
import com.ttjobs.backend.repository.InterviewScheduleRepository;
import com.ttjobs.backend.repository.JobApplicationRepository;
import com.ttjobs.backend.repository.JobRepository;
import com.ttjobs.backend.repository.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class AdminService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private CompanyRepository companyRepository;
    @Autowired
    private JobRepository jobRepository;
    @Autowired
    private JobApplicationRepository jobApplicationRepository;
    @Autowired
    private CompanyFollowRepository companyFollowRepository;
    @Autowired
    private InterviewScheduleRepository interviewScheduleRepository;
    @Autowired
    private CandidateJobMatchRepository candidateJobMatchRepository;
    @Autowired(required = false)
    private CompanyVerificationRepository companyVerificationRepository;
    @Autowired
    private RestTemplate restTemplate;
    @Autowired(required = false)
    private AdminAuditLogService adminAuditLogService;
    @Autowired(required = false)
    private AuthContextService authContextService;

    @Value("${ttjobs.ai.base-url}")
    private String aiBaseUrl;

    public List<AdminUserDTO> getUsers(String role) {
        return userRepository.findAll().stream()
                .filter(user -> role == null || role.isBlank() || user.getRole().name().equalsIgnoreCase(role.trim()))
                .map(this::toAdminUserDto)
                .toList();
    }

    @Transactional
    public AdminUserDTO updateUserRole(Long id, AdminRoleUpdateRequest request) {
        User user = userRepository.findById(id).orElseThrow();
        user.setRole(User.Role.valueOf(request.getRole().trim().toUpperCase()));
        return toAdminUserDto(userRepository.save(user));
    }

    @Transactional
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    public List<CompanyDTO> getCompanies() {
        return companyRepository.findAll().stream().map(this::toCompanyDto).toList();
    }

    @Transactional
    public void deleteCompany(Long id, AdminActionRequest request) {
        Company company = companyRepository.findById(id).orElseThrow();
        company.setDeletedAt(LocalDateTime.now());
        companyRepository.save(company);
        logAdminAction("company_deleted", "COMPANY", id, reasonOf(request), "Soft delete company from admin dashboard");
    }

    @Transactional
    public CompanyDTO updateCompany(Long id, AdminCompanyUpdateRequest request) {
        Company company = companyRepository.findById(id).orElseThrow();
        if (hasText(request.getName())) {
            company.setName(request.getName().trim());
        }
        company.setDescription(trimToNull(request.getDescription()));
        company.setLocation(trimToNull(request.getLocation()));
        company.setWebsite(trimToNull(request.getWebsite()));
        company.setIndustry(trimToNull(request.getIndustry()));
        if (hasText(request.getVerificationStatus())) {
            try {
                Company.VerificationStatus nextStatus = parseVerificationStatus(request.getVerificationStatus());
                company.setVerificationStatus(nextStatus);
                syncCompanyVerification(company, nextStatus, request.getReason());
            } catch (IllegalArgumentException ex) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid company verification status");
            }
        }
        Company saved = companyRepository.save(company);
        logAdminAction("company_updated", "COMPANY", id, request.getReason(), "Updated company from admin dashboard");
        return toCompanyDto(saved);
    }

    private Company.VerificationStatus parseVerificationStatus(String value) {
        String normalized = value.trim().toUpperCase();
        if ("VERIFY".equals(normalized) || "APPROVED".equals(normalized)) {
            return Company.VerificationStatus.VERIFIED;
        }
        if ("REJECT".equals(normalized)) {
            return Company.VerificationStatus.REJECTED;
        }
        if ("SUSPEND".equals(normalized)) {
            return Company.VerificationStatus.SUSPENDED;
        }
        return Company.VerificationStatus.valueOf(normalized);
    }

    private void syncCompanyVerification(Company company, Company.VerificationStatus status, String reason) {
        if (companyVerificationRepository == null) {
            return;
        }
        CompanyVerification verification = companyVerificationRepository.findByCompanyId(company.getId()).orElseGet(() -> {
            CompanyVerification created = new CompanyVerification();
            created.setCompany(company);
            return created;
        });
        verification.setStatus(status);
        if (status == Company.VerificationStatus.PENDING) {
            verification.setReviewReason(null);
            verification.setReviewedAt(null);
            verification.setReviewedBy(null);
        } else {
            verification.setReviewReason(trimToNull(reason));
            verification.setReviewedAt(LocalDateTime.now());
            verification.setReviewedBy(authContextService == null ? null : authContextService.getCurrentUserOptional().orElse(null));
        }
        companyVerificationRepository.save(verification);
    }

    public List<JobDTO> getJobs() {
        return jobRepository.findAll().stream().map(this::toJobDto).toList();
    }

    @Transactional
    public JobDTO updateJob(Long id, AdminJobUpdateRequest request) {
        Job job = jobRepository.findById(id).orElseThrow();
        if (hasText(request.getTitle())) {
            job.setTitle(request.getTitle().trim());
        }
        job.setDescription(trimToNull(request.getDescription()));
        job.setLocation(trimToNull(request.getLocation()));
        job.setSalary(request.getSalary());
        job.setSalaryMin(request.getSalaryMin());
        job.setSalaryMax(request.getSalaryMax());
        if (hasText(request.getCurrency())) {
            job.setCurrency(request.getCurrency().trim().toUpperCase());
        }
        job.setJobType(trimToNull(request.getJobType()));
        job.setExperienceLevel(trimToNull(request.getExperienceLevel()));
        job.setCategory(trimToNull(request.getCategory()));
        if (hasText(request.getStatus())) {
            job.setStatus(request.getStatus().trim().toLowerCase());
        }
        job.setApplicationDeadline(request.getApplicationDeadline());
        Job saved = jobRepository.save(job);
        logAdminAction("job_updated", "JOB", id, request.getReason(), "Updated job from admin dashboard");
        return toJobDto(saved);
    }

    @Transactional
    public void deleteJob(Long id, AdminActionRequest request) {
        Job job = jobRepository.findById(id).orElseThrow();
        job.setStatus("closed");
        job.setDeletedAt(LocalDateTime.now());
        jobRepository.save(job);
        logAdminAction("job_deleted", "JOB", id, reasonOf(request), "Soft delete job from admin dashboard");
    }

    public AdminStatsDTO getStats() {
        return getStats(null, null);
    }

    public AdminStatsDTO getStats(LocalDateTime customFrom, LocalDateTime customTo) {
        List<User> users = userRepository.findAll();
        List<Job> jobs = jobRepository.findAll();
        List<JobApplication> applications = jobApplicationRepository.findAll();
        List<InterviewSchedule> interviews = interviewScheduleRepository.findAll();
        LocalDateTime now = LocalDateTime.now();
        AdminStatsDTO dto = new AdminStatsDTO();
        dto.setTotalUsers(users.size());
        dto.setTotalCandidates(users.stream().filter(user -> user.getRole() == User.Role.CANDIDATE).count());
        dto.setTotalRecruiters(users.stream().filter(user -> user.getRole() == User.Role.RECRUITER).count());
        dto.setTotalAdmins(users.stream().filter(user -> user.getRole() == User.Role.ADMIN).count());
        dto.setTotalCompanies(companyRepository.count());
        dto.setTotalJobs(jobs.size());
        dto.setTotalApplications(applications.size());
        dto.setTotalInterviews(interviews.size());
        dto.setPendingInterviews(countStatus(interviews, "pending"));
        dto.setUpcomingInterviews(interviews.stream()
                .filter(interview -> interview.getScheduledAt() != null && interview.getScheduledAt().isAfter(now))
                .count());
        dto.setOpenJobs(countStatus(jobs, "open"));
        dto.setClosedJobs(countStatus(jobs, "closed"));
        dto.setNewUsersLast7Days(countUsersCreatedSince(users, now.minusDays(7)));
        dto.setNewUsersLast30Days(countUsersCreatedSince(users, now.minusDays(30)));
        dto.setStoredCandidateMatches(candidateJobMatchRepository.count());
        dto.setApplicationPerJobRatio(jobs.isEmpty() ? 0.0 : Math.round((applications.size() * 100.0 / jobs.size())) / 100.0);
        dto.setApplicationStatusCounts(groupStatuses(applications));
        dto.setInterviewStatusCounts(groupStatuses(interviews));
        dto.setPeriodMetrics(buildPeriodMetrics(users, jobs, applications, interviews, now));
        if (customFrom != null || customTo != null) {
            LocalDateTime safeTo = customTo == null ? now : customTo;
            LocalDateTime safeFrom = customFrom == null ? safeTo.minusDays(30) : customFrom;
            if (safeFrom.isAfter(safeTo)) {
                LocalDateTime tmp = safeFrom;
                safeFrom = safeTo;
                safeTo = tmp;
            }
            dto.setCustomPeriodMetrics(buildCustomPeriodMetrics(users, jobs, applications, interviews, safeFrom, safeTo));
        }
        enrichAiHealth(dto);
        return dto;
    }

    private AdminPeriodMetricsDTO buildCustomPeriodMetrics(
            List<User> users,
            List<Job> jobs,
            List<JobApplication> applications,
            List<InterviewSchedule> interviews,
            LocalDateTime from,
            LocalDateTime to
    ) {
        AdminPeriodMetricsDTO item = new AdminPeriodMetricsDTO();
        item.setKey("custom");
        item.setLabel("Từ " + from.toLocalDate() + " đến " + to.toLocalDate());
        item.setNewUsers(users.stream().filter(user -> isBetween(user.getCreatedAt(), from, to)).count());
        item.setNewJobs(jobs.stream().filter(job -> isBetween(job.getPostedDate(), from, to)).count());
        item.setNewApplications(applications.stream().filter(application -> isBetween(application.getApplicationDate(), from, to)).count());
        item.setNewInterviews(interviews.stream().filter(interview -> isBetween(interview.getCreatedAt(), from, to)).count());
        item.setScheduledInterviews(interviews.stream().filter(interview -> isBetween(interview.getScheduledAt(), from, to)).count());
        return item;
    }

    private Map<String, AdminPeriodMetricsDTO> buildPeriodMetrics(
            List<User> users,
            List<Job> jobs,
            List<JobApplication> applications,
            List<InterviewSchedule> interviews,
            LocalDateTime now
    ) {
        Map<String, LocalDateTime> starts = new LinkedHashMap<>();
        starts.put("day", now.minusDays(1));
        starts.put("week", now.minusWeeks(1));
        starts.put("month", now.minusMonths(1));
        starts.put("year", now.minusYears(1));

        Map<String, String> labels = Map.of(
                "day", "24 giờ qua",
                "week", "7 ngày qua",
                "month", "30 ngày qua",
                "year", "12 tháng qua"
        );

        Map<String, AdminPeriodMetricsDTO> metrics = new LinkedHashMap<>();
        starts.forEach((key, start) -> {
            AdminPeriodMetricsDTO item = new AdminPeriodMetricsDTO();
            item.setKey(key);
            item.setLabel(labels.getOrDefault(key, key));
            item.setNewUsers(countUsersCreatedSince(users, start));
            item.setNewJobs(jobs.stream().filter(job -> isOnOrAfter(job.getPostedDate(), start)).count());
            item.setNewApplications(applications.stream().filter(application -> isOnOrAfter(application.getApplicationDate(), start)).count());
            item.setNewInterviews(interviews.stream().filter(interview -> isOnOrAfter(interview.getCreatedAt(), start)).count());
            item.setScheduledInterviews(interviews.stream().filter(interview -> isOnOrAfter(interview.getScheduledAt(), start)).count());
            metrics.put(key, item);
        });
        return metrics;
    }

    private long countUsersCreatedSince(List<User> users, LocalDateTime since) {
        return users.stream()
                .filter(user -> user.getCreatedAt() != null && !user.getCreatedAt().isBefore(since))
                .count();
    }

    private boolean isOnOrAfter(LocalDateTime value, LocalDateTime start) {
        return value != null && !value.isBefore(start);
    }

    private boolean isBetween(LocalDateTime value, LocalDateTime from, LocalDateTime to) {
        return value != null && !value.isBefore(from) && !value.isAfter(to);
    }

    private long countStatus(List<?> values, String expectedStatus) {
        return values.stream()
                .map(this::extractStatus)
                .filter(status -> status.equalsIgnoreCase(expectedStatus))
                .count();
    }

    private Map<String, Long> groupStatuses(List<?> values) {
        return values.stream()
                .map(this::extractStatus)
                .filter(status -> !status.isBlank())
                .collect(Collectors.groupingBy(status -> status, Collectors.counting()));
    }

    private String extractStatus(Object value) {
        if (value instanceof Job job) {
            return normalizeStatus(job.getStatus());
        }
        if (value instanceof JobApplication application) {
            return normalizeStatus(application.getStatus());
        }
        if (value instanceof InterviewSchedule interview) {
            return normalizeStatus(interview.getStatus());
        }
        return "";
    }

    private String normalizeStatus(String status) {
        return Objects.toString(status, "unknown").trim().toLowerCase();
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private String trimToNull(String value) {
        return hasText(value) ? value.trim() : null;
    }

    private String reasonOf(AdminActionRequest request) {
        if (request == null) {
            return null;
        }
        return hasText(request.getReason()) ? request.getReason().trim() : request.getNote();
    }

    private void logAdminAction(String action, String targetType, Long targetId, String reason, String metadata) {
        if (adminAuditLogService != null) {
            adminAuditLogService.log(action, targetType, targetId, reason, metadata);
        }
    }

    private void enrichAiHealth(AdminStatsDTO dto) {
        dto.setAiCheckedAt(LocalDateTime.now());
        try {
            JsonNode health = restTemplate.getForObject(aiBaseUrl + "/health", JsonNode.class);
            dto.setAiServiceStatus(textValue(health, "status", "ok"));
            dto.setAiClassifierReady(booleanValue(health, "classifierReady"));
            dto.setAiMatcherReady(booleanValue(health, "matcherReady"));
            dto.setAiServiceMessage("AI service reachable");
        } catch (Exception ex) {
            dto.setAiServiceStatus("degraded");
            dto.setAiClassifierReady(false);
            dto.setAiMatcherReady(false);
            dto.setAiServiceMessage("AI service unavailable: " + ex.getClass().getSimpleName());
        }
    }

    private String textValue(JsonNode node, String fieldName, String fallback) {
        if (node == null || node.path(fieldName).isMissingNode()) {
            return fallback;
        }
        return node.path(fieldName).asText(fallback);
    }

    private Boolean booleanValue(JsonNode node, String fieldName) {
        if (node == null || node.path(fieldName).isMissingNode()) {
            return null;
        }
        return node.path(fieldName).asBoolean(false);
    }

    private AdminUserDTO toAdminUserDto(User user) {
        AdminUserDTO dto = new AdminUserDTO();
        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        dto.setRole(user.getRole() != null ? user.getRole().name() : null);
        dto.setPhone(user.getPhone());
        dto.setCreatedAt(user.getCreatedAt());
        return dto;
    }

    private CompanyDTO toCompanyDto(Company company) {
        CompanyDTO dto = new CompanyDTO();
        dto.setId(company.getId());
        dto.setName(company.getName());
        dto.setDescription(company.getDescription());
        dto.setLocation(company.getLocation());
        dto.setWebsite(company.getWebsite());
        dto.setIndustry(company.getIndustry());
        dto.setLogoUrl(company.getLogoUrl());
        dto.setVerificationStatus(company.getVerificationStatus() == null ? "PENDING" : company.getVerificationStatus().name());
        dto.setJobCount(company.getId() == null ? 0L : jobRepository.countByCompanyIdAndStatus(company.getId(), "open"));
        dto.setSavedJobCount(company.getId() == null ? 0L : jobRepository.countSavedJobsByCompanyId(company.getId()));
        dto.setFollowerCount(company.getId() == null ? 0L : companyFollowRepository.countByCompanyId(company.getId()));
        return dto;
    }

    private JobDTO toJobDto(Job job) {
        JobDTO dto = new JobDTO();
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
        dto.setImageUrl(job.getImageUrl());
        dto.setStatus(job.getStatus());
        dto.setPostedDate(job.getPostedDate());
        dto.setApplicationDeadline(job.getApplicationDeadline());
        if (job.getCompany() != null) {
            dto.setCompanyId(job.getCompany().getId());
            dto.setCompanyName(job.getCompany().getName());
            dto.setCompanyLogoUrl(job.getCompany().getLogoUrl());
        }
        return dto;
    }
}

