package com.ttjobs.backend.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.ttjobs.backend.dto.application.ApplicationTimelineDTO;
import com.ttjobs.backend.dto.application.JobApplicationDTO;
import com.ttjobs.backend.entity.CompanyMember;
import com.ttjobs.backend.entity.Job;
import com.ttjobs.backend.entity.JobApplication;
import com.ttjobs.backend.entity.JobApplicationStatusAudit;
import com.ttjobs.backend.entity.User;
import com.ttjobs.backend.entity.UserCv;
import com.ttjobs.backend.repository.JobApplicationRepository;
import com.ttjobs.backend.repository.JobApplicationStatusAuditRepository;
import com.ttjobs.backend.repository.JobRepository;
import com.ttjobs.backend.repository.UserCvRepository;
import com.ttjobs.backend.repository.UserRepository;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import jakarta.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
    // CV limits and rules aligned with UserCvService.
    private static final long MAX_CV_SIZE = 5L * 1024 * 1024;
    private static final long STREAM_MAX_SIZE = 10L * 1024 * 1024;
    private static final int DOWNLOAD_CONNECT_TIMEOUT_MS = 3000;
    private static final int DOWNLOAD_READ_TIMEOUT_MS = 5000;
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
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
    private RecruiterActivityLogService recruiterActivityLogService;

    @Autowired
    private EmailService emailService;
    @Autowired
    private ObjectProvider<Cloudinary> cloudinaryProvider;
    @Autowired(required = false)
    private AiMonitoringService aiMonitoringService;
    @Autowired
    private CvTextExtractionService cvTextExtractionService;
    @Autowired
    private UserCvRepository userCvRepository;

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

    public List<JobApplicationDTO> getMyApplications() {
        User currentUser = authContextService.requireCurrentUser();
        if (currentUser.getRole() != User.Role.CANDIDATE && !authContextService.isAdmin(currentUser)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only candidate can view applications");
        }

        return jobApplicationRepository.findByUserId(currentUser.getId()).stream()
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

    public JobApplicationDTO applyForJob(Long jobId, MultipartFile file, boolean useProfileCv, boolean saveToCvList) {
        return applyForJob(jobId, file, null, useProfileCv, false, saveToCvList, null);
    }

    public JobApplicationDTO applyForJob(Long jobId, MultipartFile file, boolean useProfileCv, boolean saveToCvList, String coverLetter) {
        return applyForJob(jobId, file, null, useProfileCv, false, saveToCvList, coverLetter);
    }

    public JobApplicationDTO applyForJob(Long jobId, MultipartFile file, Long cvId, boolean useProfileCv,
                                         boolean useSystemCv, boolean saveToCvList, String coverLetter) {
        User currentUser = authContextService.requireCurrentUser();

        if (currentUser.getRole() != User.Role.CANDIDATE) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only candidate can apply for jobs");
        }
        Optional<JobApplication> existingApplication = jobApplicationRepository.findByUserIdAndJobId(currentUser.getId(), jobId);
        if (existingApplication.isPresent()) {
            JobApplication existing = existingApplication.get();
            if (WITHDRAWN.equals(normalizeStatus(existing.getStatus()))) {
                return reapplyForWithdrawnApplication(existing, currentUser, file, cvId, useProfileCv, useSystemCv, saveToCvList, coverLetter);
            }
            throw new ResponseStatusException(HttpStatus.CONFLICT, "User has already applied for this job");
        }

        User user = userRepository.findById(currentUser.getId())
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
        application.setCoverLetter(coverLetter == null || coverLetter.isBlank() ? null : coverLetter.trim());
        // Attach CV snapshot based on upload or saved CV list.
        attachCvSnapshot(application, user, file, cvId, useProfileCv, useSystemCv, saveToCvList);
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
        recordAiEvent(user, job, "application_created", saved.getCvTextSnapshot());
        return convertToDTO(saved);
    }

    private JobApplicationDTO reapplyForWithdrawnApplication(JobApplication application, User currentUser,
                                                             MultipartFile file, Long cvId, boolean useProfileCv,
                                                             boolean useSystemCv, boolean saveToCvList,
                                                             String coverLetter) {
        User user = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        Job job = jobRepository.findByIdAndDeletedAtIsNull(application.getJob().getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Job not found"));

        if (!"open".equalsIgnoreCase(job.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Job is not open for application");
        }
        if (job.getApplicationDeadline() != null && LocalDateTime.now().isAfter(job.getApplicationDeadline())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Application deadline has passed");
        }

        application.setUser(user);
        application.setJob(job);
        application.setApplicationDate(LocalDateTime.now());
        application.setStatus(SUBMITTED);
        application.setCoverLetter(coverLetter == null || coverLetter.isBlank() ? null : coverLetter.trim());
        application.setCv(null);
        application.setCvUrl(null);
        application.setCvFileName(null);
        application.setCvTextSnapshot(null);
        attachCvSnapshot(application, user, file, cvId, useProfileCv, useSystemCv, saveToCvList);

        JobApplication saved = jobApplicationRepository.save(application);
        logStatusChange(saved, currentUser, WITHDRAWN, SUBMITTED);
        notificationService.createNotification(
                user,
                "Application resubmitted",
                "You have successfully reapplied to " + job.getTitle(),
                "APPLICATION_SUBMITTED"
        );
        emailService.sendApplicationSubmitted(user, job);
        if (job.getCompany() != null && job.getCompany().getCreatedBy() != null) {
            notificationService.createNotification(
                    job.getCompany().getCreatedBy(),
                    "Job application resubmitted",
                    user.getName() + " reapplied to " + job.getTitle(),
                    "NEW_APPLICATION"
            );
            emailService.sendNewApplication(job.getCompany().getCreatedBy(), user, job);
        }
        recordAiEvent(user, job, "application_resubmitted", saved.getCvTextSnapshot());
        return convertToDTO(saved);
    }

    public void streamCv(Long applicationId, HttpServletResponse response) {
        User currentUser = authContextService.requireCurrentUser();
        JobApplication application = jobApplicationRepository.findByIdWithDetails(applicationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Application not found"));

        requireRecruiterOwnership(currentUser, application.getJob());

        if ((application.getCvUrl() == null || application.getCvUrl().isBlank())
                && (application.getCvTextSnapshot() == null || application.getCvTextSnapshot().isBlank())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No CV attached to this application");
        }

        recruiterActivityLogService.logCvViewed(currentUser, application);
        if (application.getCvUrl() != null && !application.getCvUrl().isBlank()) {
            streamFromUrl(application.getCvUrl(), application.getCvFileName(), response);
        } else {
            streamTextCv(application.getCvTextSnapshot(), application.getCvFileName(), response);
        }
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
        recruiterActivityLogService.logApplicationStatusChanged(currentUser, saved, currentStatus, targetStatus);
        notificationService.createNotification(
                saved.getUser(),
                "Application status updated",
                "Your application for " + saved.getJob().getTitle() + " is now " + targetStatus,
                "APPLICATION_STATUS_UPDATED"
        );
        emailService.sendApplicationStatusChanged(saved.getUser(), saved.getJob(), targetStatus);
        if (SHORTLISTED.equals(targetStatus)) {
            recordAiEvent(saved.getUser(), saved.getJob(), "recruiter_shortlisted", saved.getCvTextSnapshot());
        } else if (REJECTED.equals(targetStatus)) {
            recordAiEvent(saved.getUser(), saved.getJob(), "recruiter_rejected", saved.getCvTextSnapshot());
        }
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

    public List<ApplicationTimelineDTO> getApplicationTimeline(Long applicationId) {
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
                    ApplicationTimelineDTO dto = new ApplicationTimelineDTO();
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
        dto.setCoverLetter(application.getCoverLetter());
        dto.setHasCv((application.getCvUrl() != null && !application.getCvUrl().isBlank())
                || (application.getCvTextSnapshot() != null && !application.getCvTextSnapshot().isBlank()));
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

    private void attachCvSnapshot(JobApplication application, User user, MultipartFile file,
                                  Long cvId, boolean useProfileCv, boolean useSystemCv, boolean saveToCvList) {
        boolean hasFile = file != null && !file.isEmpty();
        boolean hasSavedCvSelection = cvId != null || useProfileCv || useSystemCv;
        if (!hasFile && !hasSavedCvSelection) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Must provide a CV file or choose a saved CV");
        }
        if (hasFile && hasSavedCvSelection) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Choose either file upload or saved CV");
        }
        if (hasFile) {
            validateCvFile(file);
            Cloudinary cloudinary = requireCloudinary();
            try {
                Map<?, ?> result = cloudinary.uploader().upload(
                        file.getBytes(),
                        ObjectUtils.asMap(
                                "folder", "ttjobs/cv/applications",
                                "resource_type", "raw",
                                "public_id", "app-" + user.getId() + "-" + System.currentTimeMillis(),
                                "overwrite", true
                        )
                );
                String cvUrl = (String) result.get("secure_url");
                if (cvUrl == null || cvUrl.isBlank()) {
                    throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Cloud upload failed");
                }
                application.setCvUrl(cvUrl);
                application.setCvFileName(file.getOriginalFilename());

                // Extract text for recommendation use.
                String cvText = cvTextExtractionService.extractText(
                        file.getBytes(),
                        file.getContentType(),
                        file.getOriginalFilename()
                );
                user.setCvText(cvText);
                userRepository.save(user);

                if (saveToCvList) {
                    UserCv savedCv = saveUserCv(user, cvUrl, file.getOriginalFilename());
                    application.setCv(savedCv);
                }
                return;
            } catch (IOException e) {
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Cloud upload failed");
            }
        }

        if (useSystemCv) {
            String cvText = buildSystemCvText(user);
            if (cvText.isBlank()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "System CV is empty");
            }
            application.setCvTextSnapshot(cvText);
            application.setCvFileName("ttjobs-system-cv-" + user.getId() + ".txt");
            user.setCvText(cvText);
            userRepository.save(user);
            return;
        }

        UserCv selectedCv = cvId != null
                ? userCvRepository.findByIdAndUserId(cvId, user.getId())
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Saved CV not found"))
                : userCvRepository.findTopByUserIdOrderByUploadedAtDesc(user.getId())
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "No saved CV found"));
        application.setCvUrl(selectedCv.getCvUrl());
        application.setCvFileName(selectedCv.getFileName());
        application.setCv(selectedCv);

        if (user.getCvText() == null || user.getCvText().isBlank()) {
            byte[] data = downloadCvBytes(selectedCv.getCvUrl());
            String cvText = cvTextExtractionService.extractText(data, null, selectedCv.getFileName());
            user.setCvText(cvText);
            userRepository.save(user);
        }
    }

    private String buildSystemCvText(User user) {
        return java.util.stream.Stream.of(
                        "Ho ten: " + Objects.requireNonNullElse(user.getName(), ""),
                        "Email: " + Objects.requireNonNullElse(user.getEmail(), ""),
                        "So dien thoai: " + Objects.requireNonNullElse(user.getPhone(), ""),
                        "Vi tri ung tuyen: " + Objects.requireNonNullElse(user.getCvRole(), ""),
                        "Muc tieu nghe nghiep: " + Objects.requireNonNullElse(user.getCvObjective(), ""),
                        "Kinh nghiem noi bat: " + Objects.requireNonNullElse(user.getCvExperienceHighlights(), ""),
                        "Ky nang: " + (user.getSkills() == null ? "" : user.getSkills().stream()
                                .map(skill -> skill.getName())
                                .filter(Objects::nonNull)
                                .collect(Collectors.joining(", ")))
                )
                .filter(line -> line.substring(line.indexOf(':') + 1).trim().length() > 0)
                .collect(Collectors.joining("\n"));
    }

    private void validateCvFile(MultipartFile file) {
        if (file.getSize() > MAX_CV_SIZE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "CV file size exceeds 5MB");
        }
        if (!ALLOWED_CONTENT_TYPES.contains(file.getContentType())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only PDF/DOC/DOCX files are allowed");
        }
        validateCvSignature(file);
    }

    private Cloudinary requireCloudinary() {
        Cloudinary cloudinary = cloudinaryProvider.getIfAvailable();
        if (cloudinary == null) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Cloudinary is not configured");
        }
        return cloudinary;
    }

    private UserCv saveUserCv(User user, String cvUrl, String fileName) {
        UserCv userCv = new UserCv();
        userCv.setUser(user);
        userCv.setCvUrl(cvUrl);
        userCv.setFileName(fileName);
        return userCvRepository.save(userCv);
    }

    private void streamFromUrl(String cvUrl, String fileName, HttpServletResponse response) {
        try {
            java.net.URLConnection connection = new java.net.URL(cvUrl).openConnection();
            connection.setConnectTimeout(DOWNLOAD_CONNECT_TIMEOUT_MS);
            connection.setReadTimeout(DOWNLOAD_READ_TIMEOUT_MS);
            String contentType = inferCvContentType(fileName, connection.getContentType());
            if (contentType == null || contentType.isBlank()) {
                contentType = "application/octet-stream";
            }
            response.setContentType(contentType);
            String safeName = sanitizeCvDownloadName(fileName, contentType);
            response.setHeader("Content-Disposition", buildInlineDisposition(safeName));

            try (java.io.InputStream input = connection.getInputStream();
                 java.io.OutputStream output = response.getOutputStream()) {
                byte[] buffer = new byte[8192];
                int read;
                long total = 0;
                while ((read = input.read(buffer)) != -1) {
                    total += read;
                    if (total > STREAM_MAX_SIZE) {
                        throw new ResponseStatusException(HttpStatus.PAYLOAD_TOO_LARGE, "CV file size exceeds 10MB");
                    }
                    output.write(buffer, 0, read);
                }
                output.flush();
            }
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Failed to stream CV");
        }
    }

    private String buildInlineDisposition(String fileName) {
        String encoded = URLEncoder.encode(fileName, StandardCharsets.UTF_8).replace("+", "%20");
        return "inline; filename=\"" + fileName.replace("\"", "") + "\"; filename*=UTF-8''" + encoded;
    }

    private void streamTextCv(String cvText, String fileName, HttpServletResponse response) {
        try {
            String safeName = (fileName == null || fileName.isBlank()) ? "ttjobs-system-cv.txt" : fileName.trim();
            if (!safeName.toLowerCase().endsWith(".txt")) {
                safeName = safeName + ".txt";
            }
            safeName = safeName.replace("\r", "").replace("\n", "");
            response.setContentType("text/plain; charset=UTF-8");
            response.setHeader("Content-Disposition", buildInlineDisposition(safeName));
            response.getOutputStream().write(cvText.getBytes(StandardCharsets.UTF_8));
            response.getOutputStream().flush();
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Failed to stream CV");
        }
    }

    private String sanitizeCvDownloadName(String fileName, String contentType) {
        String safeName = (fileName == null || fileName.isBlank()) ? "cv" : fileName.trim();
        if (!safeName.contains(".")) {
            safeName = safeName + guessCvExtension(contentType);
        }
        return safeName.replace("\r", "").replace("\n", "");
    }

    private String inferCvContentType(String fileName, String fallbackContentType) {
        String lowerName = fileName == null ? "" : fileName.toLowerCase();
        if (lowerName.endsWith(".pdf")) {
            return "application/pdf";
        }
        if (lowerName.endsWith(".doc")) {
            return "application/msword";
        }
        if (lowerName.endsWith(".docx")) {
            return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
        }
        return fallbackContentType;
    }

    private String guessCvExtension(String contentType) {
        String type = contentType == null ? "" : contentType.toLowerCase();
        if (type.contains("pdf")) {
            return ".pdf";
        }
        if (type.contains("officedocument.wordprocessingml.document")) {
            return ".docx";
        }
        if (type.contains("msword")) {
            return ".doc";
        }
        return ".pdf";
    }

    private byte[] downloadCvBytes(String cvUrl) {
        try {
            java.net.URLConnection connection = new java.net.URL(cvUrl).openConnection();
            connection.setConnectTimeout(DOWNLOAD_CONNECT_TIMEOUT_MS);
            connection.setReadTimeout(DOWNLOAD_READ_TIMEOUT_MS);
            try (java.io.InputStream input = connection.getInputStream()) {
                return readLimited(input, MAX_CV_SIZE);
            }
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Failed to download CV");
        }
    }

    private byte[] readLimited(java.io.InputStream input, long maxBytes) throws IOException {
        java.io.ByteArrayOutputStream output = new java.io.ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        int read;
        long total = 0;
        while ((read = input.read(buffer)) != -1) {
            total += read;
            if (total > maxBytes) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "CV file size exceeds 5MB");
            }
            output.write(buffer, 0, read);
        }
        return output.toByteArray();
    }

    private void validateCvSignature(MultipartFile file) {
        try (InputStream input = file.getInputStream()) {
            byte[] header = input.readNBytes(8);
            String contentType = file.getContentType();
            boolean valid = switch (contentType == null ? "" : contentType) {
                case "application/pdf" -> startsWith(header, 0x25, 0x50, 0x44, 0x46);
                case "application/vnd.openxmlformats-officedocument.wordprocessingml.document" ->
                        startsWith(header, 0x50, 0x4B);
                case "application/msword" -> startsWith(header, 0xD0, 0xCF, 0x11, 0xE0);
                default -> false;
            };
            if (!valid) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid CV file content");
            }
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid CV file content");
        }
    }

    private boolean startsWith(byte[] data, int... expected) {
        if (data.length < expected.length) {
            return false;
        }
        for (int i = 0; i < expected.length; i++) {
            if ((data[i] & 0xFF) != expected[i]) {
                return false;
            }
        }
        return true;
    }

    private void recordAiEvent(User user, Job job, String eventType, String cvText) {
        if (aiMonitoringService != null) {
            aiMonitoringService.recordMatchEvent(
                    user == null ? null : user.getId(),
                    job,
                    eventType,
                    cvText,
                    job == null ? null : job.getDescription(),
                    null,
                    null,
                    "application-flow"
            );
        }
    }
}

