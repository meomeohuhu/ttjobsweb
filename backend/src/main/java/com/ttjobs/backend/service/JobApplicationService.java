package com.ttjobs.backend.service;

import com.ttjobs.backend.dto.JobApplicationDTO;
import com.ttjobs.backend.entity.CompanyMember;
import com.ttjobs.backend.entity.Job;
import com.ttjobs.backend.entity.JobApplication;
import com.ttjobs.backend.entity.JobApplicationStatusAudit;
import com.ttjobs.backend.entity.User;
import com.ttjobs.backend.repository.JobApplicationRepository;
import com.ttjobs.backend.repository.JobApplicationStatusAuditRepository;
import com.ttjobs.backend.repository.JobRepository;
import com.ttjobs.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class JobApplicationService {

    private static final String SUBMITTED = "submitted";
    private static final String REVIEWING = "reviewing";
    private static final String SHORTLISTED = "shortlisted";
    private static final String INTERVIEWED = "interviewed";
    private static final String OFFERED = "offered";
    private static final String HIRED = "hired";
    private static final String REJECTED = "rejected";
    private static final String WITHDRAWN = "withdrawn";

    private static final Set<String> RECRUITER_STATUS = Set.of(
            REVIEWING, SHORTLISTED, INTERVIEWED, OFFERED, HIRED, REJECTED
    );

    @Autowired
    private JobApplicationRepository jobApplicationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private AuthContextService authContextService;

    @Autowired
    private CompanyAuthorizationService companyAuthorizationService;

    @Autowired
    private JobApplicationStatusAuditRepository statusAuditRepository;

    @Autowired
    private NotificationService notificationService;
    @Autowired
    private EmailService emailService;

    public List<JobApplicationDTO> getAllApplications() {
        User currentUser = authContextService.requireCurrentUser();
        if (!authContextService.isAdmin(currentUser)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only admin can access all applications");
        }

        return jobApplicationRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<JobApplicationDTO> getApplicationsByUserId(Long userId) {
        User currentUser = authContextService.requireCurrentUser();
        if (!authContextService.isAdmin(currentUser) && !currentUser.getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only view your applications");
        }

        return jobApplicationRepository.findByUserId(userId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<JobApplicationDTO> getApplicationsByJobId(Long jobId) {
        User currentUser = authContextService.requireCurrentUser();

        Job job = jobRepository.findByIdAndDeletedAtIsNull(jobId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Job not found"));

        requireRecruiterOwnership(currentUser, job);

        return jobApplicationRepository.findByJobId(jobId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // Recruiter gets applications for all owned jobs. Admin can see all.
    public List<JobApplicationDTO> getApplicationsForMyJobs() {
        User currentUser = authContextService.requireCurrentUser();

        if (authContextService.isAdmin(currentUser)) {
            return jobApplicationRepository.findAll().stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
        }

        if (currentUser.getRole() != User.Role.RECRUITER) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only recruiter can view recruiter applications");
        }

        List<Long> recruiterJobIds = jobRepository.findManagedJobsByRecruiterId(
                        currentUser.getId(),
                        List.of(CompanyMember.MemberRole.RECRUITER, CompanyMember.MemberRole.ADMIN)
                ).stream()
                .map(Job::getId)
                .collect(Collectors.toList());

        if (recruiterJobIds.isEmpty()) {
            return List.of();
        }

        return jobApplicationRepository.findByJobIdIn(recruiterJobIds).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public JobApplicationDTO applyForJob(Long userId, Long jobId) {
        User currentUser = authContextService.requireCurrentUser();

        if (currentUser.getRole() != User.Role.CANDIDATE) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only candidate can apply for jobs");
        }

        if (!currentUser.getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only apply for yourself");
        }

        Optional<JobApplication> existingApplication = jobApplicationRepository.findByUserIdAndJobId(userId, jobId);
        if (existingApplication.isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "User has already applied for this job");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        Job job = jobRepository.findByIdAndDeletedAtIsNull(jobId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Job not found"));

        if (!"open".equalsIgnoreCase(job.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Job is not open for application");
        }

        if (job.getApplicationDeadline() != null && LocalDateTime.now().isAfter(job.getApplicationDeadline())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Application deadline has passed");
        }

        JobApplication application = new JobApplication();
        application.setUser(user);
        application.setJob(job);
        application.setApplicationDate(LocalDateTime.now());
        application.setStatus(SUBMITTED);
        JobApplication saved = jobApplicationRepository.save(application);
        logStatusChange(saved, currentUser, null, SUBMITTED);
        notificationService.createNotification(
                user,
                "Application submitted",
                "You have successfully applied to " + job.getTitle(),
                "APPLICATION_SUBMITTED"
        );
        // Send email to candidate after successful application.
        emailService.sendApplicationSubmitted(user, job);
        if (job.getCompany() != null && job.getCompany().getCreatedBy() != null) {
            notificationService.createNotification(
                    job.getCompany().getCreatedBy(),
                    "New job application",
                    user.getName() + " applied to " + job.getTitle(),
                    "NEW_APPLICATION"
            );
            // Send email to company owner about the new application.
            emailService.sendNewApplication(job.getCompany().getCreatedBy(), user, job);
        }
        return convertToDTO(saved);
    }

    public JobApplicationDTO updateApplicationStatus(Long applicationId, String status) {
        User currentUser = authContextService.requireCurrentUser();

        JobApplication application = jobApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Application not found"));

        requireRecruiterOwnership(currentUser, application.getJob());

        String targetStatus = normalizeStatus(status);
        if (!RECRUITER_STATUS.contains(targetStatus)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Recruiter cannot set this status");
        }

        String currentStatus = normalizeStatus(application.getStatus());
        validateApplicationTransition(currentStatus, targetStatus);
        application.setStatus(targetStatus);
        JobApplication saved = jobApplicationRepository.save(application);
        logStatusChange(saved, currentUser, currentStatus, targetStatus);
        notificationService.createNotification(
                saved.getUser(),
                "Application status updated",
                "Your application for " + saved.getJob().getTitle() + " is now " + targetStatus,
                "APPLICATION_STATUS_UPDATED"
        );
        return convertToDTO(saved);
    }

    public void deleteApplication(Long applicationId) {
        // Keep endpoint contract: DELETE acts as candidate withdraw, no hard delete.
        withdrawApplication(applicationId);
    }

    public JobApplicationDTO withdrawApplication(Long applicationId) {
        User currentUser = authContextService.requireCurrentUser();

        JobApplication application = jobApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Application not found"));

        if (!currentUser.getId().equals(application.getUser().getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only withdraw your own application");
        }

        String currentStatus = normalizeStatus(application.getStatus());
        if (isTerminal(currentStatus)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Terminal status cannot be changed");
        }

        application.setStatus(WITHDRAWN);
        JobApplication saved = jobApplicationRepository.save(application);
        logStatusChange(saved, currentUser, currentStatus, WITHDRAWN);
        return convertToDTO(saved);
    }

    public List<com.ttjobs.backend.dto.ApplicationTimelineDTO> getApplicationTimeline(Long applicationId) {
        User currentUser = authContextService.requireCurrentUser();
        JobApplication application = jobApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Application not found"));

        if (!authContextService.isAdmin(currentUser)
                && !currentUser.getId().equals(application.getUser().getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only view your own application timeline");
        }

        return statusAuditRepository.findByApplicationIdOrderByChangedAtAsc(applicationId)
                .stream()
                .map(audit -> {
                    com.ttjobs.backend.dto.ApplicationTimelineDTO dto =
                            new com.ttjobs.backend.dto.ApplicationTimelineDTO();
                    dto.setFromStatus(audit.getFromStatus());
                    dto.setToStatus(audit.getToStatus());
                    dto.setChangedAt(audit.getChangedAt());
                    return dto;
                })
                .collect(java.util.stream.Collectors.toList());
    }

    private String normalizeStatus(String status) {
        if (status == null || status.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Status is required");
        }
        return status.trim().toLowerCase();
    }

    private boolean isTerminal(String status) {
        return HIRED.equals(status) || REJECTED.equals(status) || WITHDRAWN.equals(status);
    }

    private void validateApplicationTransition(String currentStatus, String targetStatus) {
        String from = normalizeStatus(currentStatus);

        if (isTerminal(from)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Terminal status cannot be changed");
        }

        boolean valid = switch (from) {
            case SUBMITTED -> targetStatus.equals(REVIEWING) || targetStatus.equals(REJECTED);
            case REVIEWING -> targetStatus.equals(SHORTLISTED) || targetStatus.equals(INTERVIEWED) || targetStatus.equals(REJECTED);
            case SHORTLISTED -> targetStatus.equals(INTERVIEWED) || targetStatus.equals(REJECTED);
            case INTERVIEWED -> targetStatus.equals(OFFERED) || targetStatus.equals(REJECTED);
            case OFFERED -> targetStatus.equals(HIRED) || targetStatus.equals(REJECTED);
            default -> false;
        };

        if (!valid) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid application status transition");
        }
    }

    private void requireRecruiterOwnership(User currentUser, Job job) {
        if (authContextService.isAdmin(currentUser)) {
            return;
        }

        if (currentUser.getRole() != User.Role.RECRUITER) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only recruiter can manage job applications");
        }

        if (job.getCompany() == null || job.getCompany().getDeletedAt() != null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not own this job");
        }

        if (!companyAuthorizationService.canManageCompany(currentUser, job.getCompany())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not own this job");
        }
    }

    // Persist status transition for audit/debug/recruitment traceability.
    private void logStatusChange(JobApplication application, User changedBy, String fromStatus, String toStatus) {
        JobApplicationStatusAudit audit = new JobApplicationStatusAudit();
        audit.setApplication(application);
        audit.setChangedBy(changedBy);
        audit.setFromStatus(fromStatus);
        audit.setToStatus(toStatus);
        audit.setChangedAt(LocalDateTime.now());
        statusAuditRepository.save(audit);
    }

    private JobApplicationDTO convertToDTO(JobApplication application) {
        JobApplicationDTO dto = new JobApplicationDTO();
        dto.setId(application.getId());
        dto.setApplicationDate(application.getApplicationDate());
        dto.setStatus(application.getStatus());
        if (application.getUser() != null) {
            dto.setUserId(application.getUser().getId());
            dto.setUserName(application.getUser().getName());
        }
        if (application.getJob() != null) {
            dto.setJobId(application.getJob().getId());
            dto.setJobTitle(application.getJob().getTitle());
            if (application.getJob().getCompany() != null) {
                dto.setCompanyName(application.getJob().getCompany().getName());
            }
        }
        return dto;
    }
}
