package com.ttjobs.backend.service;

import com.ttjobs.backend.dto.application.ApplicationTimelineDTO;
import com.ttjobs.backend.dto.ai.AiCvScoreDTO;
import com.ttjobs.backend.dto.ai.AiCvScoreRequest;
import com.ttjobs.backend.dto.recruiter.CandidateSearchDTO;
import com.ttjobs.backend.dto.interview.InterviewScheduleDTO;
import com.ttjobs.backend.dto.interview.InterviewScheduleRequest;
import com.ttjobs.backend.dto.recruiter.RecruiterApplicationDTO;
import com.ttjobs.backend.dto.recruiter.RecruiterApplicationDetailDTO;
import com.ttjobs.backend.dto.recruiter.RecruiterCompanyDTO;
import com.ttjobs.backend.dto.recruiter.RecruiterJobDTO;
import com.ttjobs.backend.dto.recruiter.RecruiterReportDTO;
import com.ttjobs.backend.dto.recruiter.RecruitmentCampaignDTO;
import com.ttjobs.backend.dto.recruiter.RecruitmentCampaignRequest;
import com.ttjobs.backend.entity.Company;
import com.ttjobs.backend.entity.CompanyMember;
import com.ttjobs.backend.entity.ApplicationAiScore;
import com.ttjobs.backend.entity.InterviewSchedule;
import com.ttjobs.backend.entity.Job;
import com.ttjobs.backend.entity.JobApplication;
import com.ttjobs.backend.entity.JobApplicationStatusAudit;
import com.ttjobs.backend.entity.RecruitmentCampaign;
import com.ttjobs.backend.entity.User;
import com.ttjobs.backend.repository.CompanyFollowRepository;
import com.ttjobs.backend.repository.CompanyMemberRepository;
import com.ttjobs.backend.repository.CompanyRepository;
import com.ttjobs.backend.repository.ApplicationAiScoreRepository;
import com.ttjobs.backend.repository.InterviewScheduleRepository;
import com.ttjobs.backend.repository.JobApplicationRepository;
import com.ttjobs.backend.repository.JobApplicationStatusAuditRepository;
import com.ttjobs.backend.repository.JobRepository;
import com.ttjobs.backend.repository.RecruitmentCampaignRepository;
import com.ttjobs.backend.repository.SavedJobRepository;
import com.ttjobs.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
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
    private static final Set<String> VALID_CAMPAIGN_STATUSES = Set.of("active", "paused", "archived", "completed");

    @Autowired
    private AuthContextService authContextService;
    @Autowired
    private CompanyAuthorizationService companyAuthorizationService;
    @Autowired
    private CompanyRepository companyRepository;
    @Autowired
    private CompanyVerificationStatusService companyVerificationStatusService;
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
    @Autowired
    private InterviewScheduleRepository interviewScheduleRepository;
    @Autowired
    private EmailService emailService;
    @Autowired
    private RecruitmentCampaignRepository campaignRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ApplicationAiScoreRepository applicationAiScoreRepository;
    @Autowired
    private RestTemplate restTemplate;

    @Value("${ttjobs.ai.base-url}")
    private String aiBaseUrl;

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

    @Transactional
    public List<RecruiterApplicationDTO> screenManagedApplications(Long companyId, Long jobId, String status,
                                                                   String keyword, Integer minScore,
                                                                   Integer page, Integer size,
                                                                   Boolean refresh) {
        User currentUser = requireRecruiterOrAdmin();
        int safeMinScore = minScore == null ? 0 : Math.max(0, Math.min(100, minScore));
        return paginate(loadManagedApplications(currentUser).stream()
                .filter(application -> companyId == null || companyId.equals(applicationCompanyId(application)))
                .filter(application -> jobId == null || jobId.equals(applicationJobId(application)))
                .filter(application -> isBlank(status) || status.equalsIgnoreCase(application.getStatus()))
                .filter(application -> matchesApplicationKeyword(application, keyword))
                .map(application -> ensureAiScore(application, Boolean.TRUE.equals(refresh)))
                .filter(Objects::nonNull)
                .filter(application -> applicationScore(application) >= safeMinScore)
                .sorted(Comparator.comparing(this::applicationScore).reversed()
                        .thenComparing(JobApplication::getApplicationDate, Comparator.nullsLast(Comparator.reverseOrder())))
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

    @Transactional
    public RecruiterApplicationDetailDTO scoreManagedApplication(Long applicationId, Boolean refresh) {
        User currentUser = requireRecruiterOrAdmin();
        JobApplication application = jobApplicationRepository.findByIdWithDetails(applicationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Application not found"));

        requireApplicationAccess(currentUser, application);
        if (isBlank(resolveCvText(application))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Application has no CV text for AI scoring");
        }
        if (isBlank(buildJobText(application.getJob()))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Job has no description for AI scoring");
        }
        ensureAiScore(application, Boolean.TRUE.equals(refresh));
        if (applicationAiScoreRepository.findByApplicationId(application.getId()).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "AI service did not return a CV score");
        }
        return toApplicationDetailDto(application);
    }

    public List<RecruiterApplicationDTO> bulkUpdateApplicationStatus(List<Long> applicationIds, String status) {
        User currentUser = requireRecruiterOrAdmin();
        if (applicationIds == null || applicationIds.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "applicationIds is required");
        }
        if (isBlank(status)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "status is required");
        }
        return applicationIds.stream()
                .map(id -> updateApplicationStatusForRecruiter(currentUser, id, status))
                .map(this::toApplicationDto)
                .toList();
    }

    public List<CandidateSearchDTO> searchCandidates(String keyword, Integer minExperience, String status) {
        User currentUser = requireRecruiterOrAdmin();
        String value = isBlank(keyword) ? "" : keyword.toLowerCase(Locale.ROOT);
        return loadManagedApplications(currentUser).stream()
                .filter(application -> isBlank(status) || status.equalsIgnoreCase(application.getStatus()))
                .filter(application -> application.getUser() != null)
                .filter(application -> minExperience == null
                        || (application.getUser().getExperienceYears() != null
                        && application.getUser().getExperienceYears() >= minExperience))
                .filter(application -> value.isBlank()
                        || contains(application.getUser().getName(), value)
                        || contains(application.getUser().getEmail(), value)
                        || contains(application.getJob() == null ? null : application.getJob().getTitle(), value))
                .collect(Collectors.groupingBy(application -> application.getUser().getId()))
                .values()
                .stream()
                .map(items -> toCandidateDto(items.stream()
                        .max(Comparator.comparing(JobApplication::getApplicationDate, Comparator.nullsLast(Comparator.naturalOrder())))
                        .orElse(items.get(0)), items.size()))
                .sorted(Comparator.comparing(CandidateSearchDTO::getCandidateName, Comparator.nullsLast(String::compareToIgnoreCase)))
                .toList();
    }

    public RecruiterReportDTO getReport(Integer days) {
        User currentUser = requireRecruiterOrAdmin();
        int safeDays = days == null ? 30 : Math.max(1, Math.min(days, 365));
        LocalDateTime from = LocalDateTime.now().minusDays(safeDays);
        List<Job> jobs = loadManagedJobs(currentUser);
        List<JobApplication> applications = loadManagedApplications(currentUser);
        List<Long> jobIds = jobs.stream().map(Job::getId).filter(Objects::nonNull).toList();

        RecruiterReportDTO dto = new RecruiterReportDTO();
        dto.setOpenJobs(jobs.stream().filter(job -> "open".equalsIgnoreCase(job.getStatus())).count());
        dto.setTotalApplications((long) applications.size());
        dto.setNewApplications(applications.stream()
                .filter(application -> application.getApplicationDate() != null && !application.getApplicationDate().isBefore(from))
                .count());
        dto.setInterviewsScheduled(jobIds.isEmpty() ? 0L
                : interviewScheduleRepository.countByApplicationJobIdInAndScheduledAtBetween(jobIds, from, LocalDateTime.now().plusYears(1)));
        dto.setHiredApplications(applications.stream().filter(application -> "hired".equalsIgnoreCase(application.getStatus())).count());
        dto.setRejectedApplications(applications.stream().filter(application -> "rejected".equalsIgnoreCase(application.getStatus())).count());
        dto.setApplicationsByStatus(applications.stream()
                .collect(Collectors.groupingBy(application -> application.getStatus() == null ? "submitted" : application.getStatus(), Collectors.counting())));
        dto.setApplicationsByJob(applications.stream()
                .filter(application -> application.getJob() != null)
                .collect(Collectors.groupingBy(application -> application.getJob().getTitle(), Collectors.counting())));
        return dto;
    }

    public List<InterviewScheduleDTO> getManagedInterviews() {
        User currentUser = requireRecruiterOrAdmin();
        List<Long> jobIds = loadManagedJobs(currentUser).stream().map(Job::getId).filter(Objects::nonNull).toList();
        if (jobIds.isEmpty()) {
            return List.of();
        }
        return interviewScheduleRepository.findByApplicationJobIdInOrderByScheduledAtAsc(jobIds)
                .stream()
                .map(this::toInterviewDto)
                .toList();
    }

    public InterviewScheduleDTO createInterview(InterviewScheduleRequest request) {
        User currentUser = requireRecruiterOrAdmin();
        if (request == null || request.getApplicationId() == null || request.getScheduledAt() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "applicationId and scheduledAt are required");
        }
        JobApplication application = jobApplicationRepository.findByIdWithDetails(request.getApplicationId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Application not found"));
        requireApplicationAccess(currentUser, application);

        InterviewSchedule interview = new InterviewSchedule();
        interview.setApplication(application);
        interview.setRecruiter(currentUser);
        interview.setCandidate(application.getUser());
        interview.setScheduledAt(request.getScheduledAt());
        interview.setDurationMinutes(request.getDurationMinutes());
        interview.setLocation(request.getLocation());
        interview.setMeetingLink(request.getMeetingLink());
        interview.setNote(request.getNote());
        interview.setStatus(isBlank(request.getStatus()) ? "pending" : request.getStatus());
        InterviewSchedule saved = interviewScheduleRepository.save(interview);
        emailService.sendInterviewCreated(saved.getCandidate(), saved);
        return toInterviewDto(saved);
    }

    public InterviewScheduleDTO updateInterviewStatus(Long interviewId, String status) {
        User currentUser = requireRecruiterOrAdmin();
        if (isBlank(status)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "status is required");
        }
        InterviewSchedule interview = interviewScheduleRepository.findById(interviewId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Interview not found"));
        requireApplicationAccess(currentUser, interview.getApplication());
        interview.setStatus(status);
        return toInterviewDto(interviewScheduleRepository.save(interview));
    }

    public List<RecruitmentCampaignDTO> getCampaigns() {
        User currentUser = requireRecruiterOrAdmin();
        List<Long> companyIds = loadManagedCompanies(currentUser).stream().map(Company::getId).toList();
        if (companyIds.isEmpty()) {
            return List.of();
        }
        return campaignRepository.findByCompanyIdInOrderByCreatedAtDesc(companyIds).stream()
                .map(this::toCampaignDto)
                .toList();
    }

    @Transactional
    public RecruitmentCampaignDTO saveCampaign(Long campaignId, RecruitmentCampaignRequest request) {
        User currentUser = requireRecruiterOrAdmin();
        if (request == null || request.getCompanyId() == null || isBlank(request.getName())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "companyId and name are required");
        }
        Company company = companyRepository.findByIdAndDeletedAtIsNull(request.getCompanyId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Company not found"));
        if (!authContextService.isAdmin(currentUser) && !companyAuthorizationService.canManageCompany(currentUser, company)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You cannot manage this company");
        }

        RecruitmentCampaign campaign = campaignId == null
                ? new RecruitmentCampaign()
                : campaignRepository.findById(campaignId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Campaign not found"));
        if (campaign.getCompany() != null
                && !authContextService.isAdmin(currentUser)
                && !companyAuthorizationService.canManageCompany(currentUser, campaign.getCompany())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You cannot manage this campaign");
        }
        campaign.setCompany(company);
        if (campaign.getCreatedBy() == null) {
            campaign.setCreatedBy(currentUser);
        }
        campaign.setName(request.getName());
        campaign.setDescription(request.getDescription());
        campaign.setStatus(normalizeCampaignStatus(request.getStatus()));
        campaign.setTargetHires(request.getTargetHires());
        campaign.setStartsAt(request.getStartsAt());
        campaign.setEndsAt(request.getEndsAt());
        List<Job> campaignJobs = loadCampaignJobs(currentUser, request.getJobIds());
        if (campaign.getJobs() == null) {
            campaign.setJobs(new ArrayList<>());
        } else {
            campaign.getJobs().clear();
        }
        campaign.getJobs().addAll(campaignJobs);
        return toCampaignDto(campaignRepository.save(campaign));
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

    private JobApplication updateApplicationStatusForRecruiter(User currentUser, Long applicationId, String status) {
        JobApplication application = jobApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Application not found"));
        requireApplicationAccess(currentUser, application);
        application.setStatus(status.trim().toLowerCase(Locale.ROOT));
        return jobApplicationRepository.save(application);
    }

    private List<Job> loadCampaignJobs(User currentUser, List<Long> jobIds) {
        if (jobIds == null || jobIds.isEmpty()) {
            return List.of();
        }
        Map<Long, Job> managed = loadManagedJobs(currentUser).stream()
                .collect(Collectors.toMap(Job::getId, job -> job));
        return jobIds.stream()
                .map(managed::get)
                .filter(Objects::nonNull)
                .toList();
    }

    private String normalizeCampaignStatus(String status) {
        String value = isBlank(status) ? "active" : status.trim().toLowerCase(Locale.ROOT);
        if (!VALID_CAMPAIGN_STATUSES.contains(value)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid campaign status");
        }
        return value;
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
        dto.setVerificationStatus(companyVerificationStatusService.getEffectiveStatus(company).name());
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
        attachAiScore(dto, application);
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
        attachAiScore(dto, application);
        dto.setTimeline(statusAuditRepository.findByApplicationIdOrderByChangedAtAsc(application.getId())
                .stream()
                .map(this::toTimelineDto)
                .toList());
        return dto;
    }

    private CandidateSearchDTO toCandidateDto(JobApplication application, int applicationCount) {
        CandidateSearchDTO dto = new CandidateSearchDTO();
        User candidate = application.getUser();
        dto.setCandidateId(candidate.getId());
        dto.setCandidateName(candidate.getName());
        dto.setCandidateEmail(candidate.getEmail());
        dto.setCandidatePhone(candidate.getPhone());
        dto.setAddress(candidate.getAddress());
        dto.setExperienceYears(candidate.getExperienceYears());
        dto.setApplicationCount((long) applicationCount);
        dto.setLatestJobTitle(application.getJob() == null ? null : application.getJob().getTitle());
        dto.setLatestStatus(application.getStatus());
        dto.setHasCv(hasCv(application));
        return dto;
    }

    private InterviewScheduleDTO toInterviewDto(InterviewSchedule interview) {
        InterviewScheduleDTO dto = new InterviewScheduleDTO();
        dto.setId(interview.getId());
        dto.setScheduledAt(interview.getScheduledAt());
        dto.setDurationMinutes(interview.getDurationMinutes());
        dto.setLocation(interview.getLocation());
        dto.setMeetingLink(interview.getMeetingLink());
        dto.setNote(interview.getNote());
        dto.setStatus(interview.getStatus());
        dto.setCreatedAt(interview.getCreatedAt());
        JobApplication application = interview.getApplication();
        if (application != null) {
            dto.setApplicationId(application.getId());
            if (application.getUser() != null) {
                dto.setCandidateId(application.getUser().getId());
                dto.setCandidateName(application.getUser().getName());
            }
            if (application.getJob() != null) {
                dto.setJobId(application.getJob().getId());
                dto.setJobTitle(application.getJob().getTitle());
                if (application.getJob().getCompany() != null) {
                    dto.setCompanyId(application.getJob().getCompany().getId());
                    dto.setCompanyName(application.getJob().getCompany().getName());
                }
            }
        }
        return dto;
    }

    private RecruitmentCampaignDTO toCampaignDto(RecruitmentCampaign campaign) {
        RecruitmentCampaignDTO dto = new RecruitmentCampaignDTO();
        dto.setId(campaign.getId());
        if (campaign.getCompany() != null) {
            dto.setCompanyId(campaign.getCompany().getId());
            dto.setCompanyName(campaign.getCompany().getName());
        }
        dto.setName(campaign.getName());
        dto.setDescription(campaign.getDescription());
        dto.setStatus(campaign.getStatus());
        dto.setTargetHires(campaign.getTargetHires());
        dto.setStartsAt(campaign.getStartsAt());
        dto.setEndsAt(campaign.getEndsAt());
        dto.setJobCount((long) (campaign.getJobs() == null ? 0 : campaign.getJobs().size()));
        dto.setApplicationCount(campaign.getId() == null ? 0L : campaignRepository.countApplicationsByCampaignId(campaign.getId()));
        dto.setJobIds(campaign.getJobs() == null ? List.of() : campaign.getJobs().stream().map(Job::getId).toList());
        dto.setCreatedAt(campaign.getCreatedAt());
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
        return !isBlank(application.getCvUrl()) || !isBlank(application.getCvTextSnapshot());
    }

    private void attachAiScore(RecruiterApplicationDTO dto, JobApplication application) {
        applicationAiScoreRepository.findByApplicationId(application.getId()).ifPresent(score -> {
            dto.setAiScore(score.getScore());
            dto.setAiLevel(score.getLevel());
            dto.setAiScoredAt(score.getScoredAt());
            dto.setAiScoreAvailable(true);
        });
    }

    private void attachAiScore(RecruiterApplicationDetailDTO dto, JobApplication application) {
        applicationAiScoreRepository.findByApplicationId(application.getId()).ifPresent(score -> {
            dto.setAiScore(score.getScore());
            dto.setAiLevel(score.getLevel());
            dto.setAiRawScore(score.getRawScore());
            dto.setAiSignals(deserializeSignals(score.getSignals()));
            dto.setAiPros(deserializeSignals(score.getPros()));
            dto.setAiCons(deserializeSignals(score.getCons()));
            dto.setAiScoredAt(score.getScoredAt());
            dto.setAiScoreAvailable(true);
        });
    }

    private JobApplication ensureAiScore(JobApplication application, boolean refresh) {
        String cvText = resolveCvText(application);
        String jobText = buildJobText(application.getJob());
        if (isBlank(cvText) || isBlank(jobText)) {
            return application;
        }

        String cvHash = sha256(cvText);
        String jobHash = sha256(jobText);
        ApplicationAiScore existing = applicationAiScoreRepository.findByApplicationId(application.getId()).orElse(null);
        if (!refresh && existing != null
                && cvHash.equals(existing.getCvHash())
                && jobHash.equals(existing.getJobHash())) {
            return application;
        }

        AiCvScoreDTO aiScore = fetchCvScore(cvText, jobText);
        if (aiScore == null || aiScore.getScore() == null) {
            return application;
        }

        ApplicationAiScore entity = existing == null ? new ApplicationAiScore() : existing;
        entity.setApplication(application);
        entity.setScore(Math.max(1, Math.min(100, aiScore.getScore())));
        entity.setLevel(isBlank(aiScore.getLevel()) ? "possible_match" : aiScore.getLevel());
        entity.setRawScore(aiScore.getRawScore());
        entity.setSignals(serializeSignals(aiScore.getSignals()));
        entity.setPros(serializeSignals(aiScore.getPros()));
        entity.setCons(serializeSignals(aiScore.getCons()));
        entity.setCvHash(cvHash);
        entity.setJobHash(jobHash);
        entity.setScoredAt(LocalDateTime.now());
        applicationAiScoreRepository.save(entity);
        return application;
    }

    private AiCvScoreDTO fetchCvScore(String cvText, String jobText) {
        try {
            AiCvScoreRequest request = new AiCvScoreRequest();
            request.setCvText(cvText);
            request.setJobText(jobText);

            RequestEntity<AiCvScoreRequest> entity = RequestEntity
                    .post(aiBaseUrl + "/ai/score-cv")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request);

            return restTemplate.exchange(entity, AiCvScoreDTO.class).getBody();
        } catch (Exception ex) {
            return null;
        }
    }

    private String resolveCvText(JobApplication application) {
        if (!isBlank(application.getCvTextSnapshot())) {
            return application.getCvTextSnapshot();
        }
        if (application.getUser() != null && !isBlank(application.getUser().getCvText())) {
            return application.getUser().getCvText();
        }
        if (application.getUser() == null) {
            return null;
        }
        return String.join("\n",
                Objects.requireNonNullElse(application.getUser().getCvRole(), ""),
                Objects.requireNonNullElse(application.getUser().getCvObjective(), ""),
                Objects.requireNonNullElse(application.getUser().getCvExperienceHighlights(), ""),
                application.getUser().getSkills() == null ? "" : application.getUser().getSkills().stream()
                        .map(skill -> skill.getName())
                        .filter(Objects::nonNull)
                        .collect(Collectors.joining(", "))
        );
    }

    private String buildJobText(Job job) {
        if (job == null) {
            return null;
        }
        List<String> parts = new ArrayList<>();
        parts.add(job.getTitle());
        parts.add(job.getDescription());
        parts.add(job.getCategory());
        parts.add(job.getExperienceLevel());
        parts.add(job.getJobType());
        parts.add(job.getLocation());
        if (job.getCompany() != null) {
            parts.add(job.getCompany().getName());
            parts.add(job.getCompany().getIndustry());
        }
        if (job.getSkills() != null) {
            parts.add(job.getSkills().stream()
                    .map(skill -> skill.getName())
                    .filter(Objects::nonNull)
                    .collect(Collectors.joining(", ")));
        }
        return parts.stream()
                .filter(part -> part != null && !part.isBlank())
                .collect(Collectors.joining("\n"));
    }

    private int applicationScore(JobApplication application) {
        return applicationAiScoreRepository.findByApplicationId(application.getId())
                .map(ApplicationAiScore::getScore)
                .orElse(0);
    }

    private String serializeSignals(List<String> signals) {
        if (signals == null || signals.isEmpty()) {
            return null;
        }
        return signals.stream()
                .filter(signal -> signal != null && !signal.isBlank())
                .collect(Collectors.joining("\n"));
    }

    private List<String> deserializeSignals(String signals) {
        if (isBlank(signals)) {
            return List.of();
        }
        return Arrays.stream(signals.split("\\R"))
                .map(String::trim)
                .filter(signal -> !signal.isBlank())
                .toList();
    }

    private String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder();
            for (byte item : bytes) {
                builder.append(String.format("%02x", item));
            }
            return builder.toString();
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 is unavailable", ex);
        }
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

