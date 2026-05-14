package com.ttjobs.backend.service;

import com.ttjobs.backend.dto.recruiter.RecruiterActivityLogDTO;
import com.ttjobs.backend.entity.Company;
import com.ttjobs.backend.entity.Job;
import com.ttjobs.backend.entity.JobApplication;
import com.ttjobs.backend.entity.RecruiterActivityLog;
import com.ttjobs.backend.entity.User;
import com.ttjobs.backend.repository.RecruiterActivityLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class RecruiterActivityLogService {

    private static final Logger log = LoggerFactory.getLogger(RecruiterActivityLogService.class);

    @Autowired
    private AuthContextService authContextService;

    @Autowired
    private RecruiterActivityLogRepository recruiterActivityLogRepository;

    public void logLoginSuccess(User actor) {
        if (!shouldTrack(actor)) {
            return;
        }
        record(actor, null, null, null, "LOGIN_SUCCESS", "Đăng nhập thành công",
                actor.getName() == null ? "Recruiter đã đăng nhập vào workspace." : actor.getName() + " đã đăng nhập vào workspace.");
    }

    public void logJobCreated(User actor, Job job) {
        record(actor, job != null ? job.getCompany() : null, job, null, "JOB_CREATED",
                "Tạo job mới",
                buildJobDetail("Đã tạo job", job));
    }

    public void logJobUpdated(User actor, Job job) {
        record(actor, job != null ? job.getCompany() : null, job, null, "JOB_UPDATED",
                "Cập nhật job",
                buildJobDetail("Đã cập nhật job", job));
    }

    public void logJobStatusChanged(User actor, Job job, String actionType, String previousStatus, String nextStatus) {
        record(actor, job != null ? job.getCompany() : null, job, null, actionType,
                actionTitleFor(actionType, "Cập nhật trạng thái job"),
                buildJobDetail("Trạng thái job thay đổi từ " + safeText(previousStatus) + " sang " + safeText(nextStatus), job));
    }

    public void logCompanyCreated(User actor, Company company) {
        record(actor, company, null, null, "COMPANY_CREATED",
                "Tạo công ty",
                company == null ? "Đã tạo công ty mới." : "Đã tạo công ty " + safeText(company.getName()));
    }

    public void logCompanyUpdated(User actor, Company company) {
        record(actor, company, null, null, "COMPANY_UPDATED",
                "Cập nhật công ty",
                company == null ? "Đã cập nhật công ty." : "Đã cập nhật thông tin công ty " + safeText(company.getName()));
    }

    public void logCompanyDeleted(User actor, Company company) {
        record(actor, company, null, null, "COMPANY_ARCHIVED",
                "Ngừng hoạt động công ty",
                company == null ? "Đã ngừng hoạt động công ty." : "Đã ngừng hoạt động công ty " + safeText(company.getName()));
    }

    public void logCompanyMemberAdded(User actor, Company company, User targetUser, String role) {
        record(actor, company, null, null, "COMPANY_MEMBER_ADDED",
                "Thêm thành viên",
                buildMemberDetail("Đã thêm", company, targetUser, role));
    }

    public void logCompanyMemberUpdated(User actor, Company company, User targetUser, String role) {
        record(actor, company, null, null, "COMPANY_MEMBER_UPDATED",
                "Cập nhật quyền thành viên",
                buildMemberDetail("Đã cập nhật quyền cho", company, targetUser, role));
    }

    public void logCompanyMemberRemoved(User actor, Company company, User targetUser) {
        record(actor, company, null, null, "COMPANY_MEMBER_REMOVED",
                "Xóa thành viên",
                buildMemberDetail("Đã xóa", company, targetUser, null));
    }

    public void logApplicationStatusChanged(User actor, JobApplication application, String previousStatus, String nextStatus) {
        Job job = application != null ? application.getJob() : null;
        record(actor, job != null ? job.getCompany() : null, job, application, "APPLICATION_STATUS_CHANGED",
                "Duyệt CV",
                buildApplicationDetail(application, previousStatus, nextStatus));
    }

    public void logCvViewed(User actor, JobApplication application) {
        Job job = application != null ? application.getJob() : null;
        record(actor, job != null ? job.getCompany() : null, job, application, "CV_VIEWED",
                "Xem CV ứng viên",
                buildApplicationDetail(application, null, null));
    }

    public List<RecruiterActivityLogDTO> getRecentActivities(int limit) {
        User currentUser = authContextService.requireCurrentUser();
        if (!isRecruiterLike(currentUser)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only recruiter can access activity history");
        }

        int safeLimit = Math.max(1, Math.min(limit, 50));
        return recruiterActivityLogRepository.findByActorIdOrderByCreatedAtDesc(
                        currentUser.getId(),
                        PageRequest.of(0, safeLimit)
                ).stream()
                .map(this::toDto)
                .toList();
    }

    private void record(User actor, Company company, Job job, JobApplication application,
                        String actionType, String title, String details) {
        if (!shouldTrack(actor)) {
            return;
        }

        try {
            RecruiterActivityLog entry = new RecruiterActivityLog();
            entry.setActor(actor);
            entry.setCompany(company);
            entry.setJob(job);
            entry.setApplication(application);
            entry.setActionType(actionType);
            entry.setTitle(title);
            entry.setDetails(details);
            entry.setCreatedAt(LocalDateTime.now());
            recruiterActivityLogRepository.save(entry);
        } catch (RuntimeException ex) {
            log.warn("Failed to persist recruiter activity log: {}", ex.getMessage());
        }
    }

    private boolean shouldTrack(User actor) {
        return actor != null && isRecruiterLike(actor);
    }

    private boolean isRecruiterLike(User user) {
        return user.getRole() == User.Role.RECRUITER || authContextService.isAdmin(user);
    }

    private RecruiterActivityLogDTO toDto(RecruiterActivityLog entry) {
        RecruiterActivityLogDTO dto = new RecruiterActivityLogDTO();
        dto.setId(entry.getId());
        dto.setActionType(entry.getActionType());
        dto.setTitle(entry.getTitle());
        dto.setDetails(entry.getDetails());
        dto.setCreatedAt(entry.getCreatedAt());
        if (entry.getCompany() != null) {
            dto.setCompanyId(entry.getCompany().getId());
            dto.setCompanyName(entry.getCompany().getName());
        }
        if (entry.getJob() != null) {
            dto.setJobId(entry.getJob().getId());
            dto.setJobTitle(entry.getJob().getTitle());
        }
        if (entry.getApplication() != null && entry.getApplication().getUser() != null) {
            dto.setApplicationId(entry.getApplication().getId());
            dto.setCandidateName(entry.getApplication().getUser().getName());
        }
        return dto;
    }

    private String buildJobDetail(String prefix, Job job) {
        if (job == null) {
            return prefix + ".";
        }
        StringBuilder builder = new StringBuilder(prefix).append(" ").append(safeText(job.getTitle()));
        if (job.getCompany() != null) {
            builder.append(" tại ").append(safeText(job.getCompany().getName()));
        }
        if (job.getStatus() != null) {
            builder.append(" (").append(job.getStatus()).append(")");
        }
        return builder.toString();
    }

    private String buildApplicationDetail(JobApplication application, String previousStatus, String nextStatus) {
        if (application == null) {
            return "Đã thao tác trên hồ sơ ứng viên.";
        }
        String candidate = application.getUser() != null ? safeText(application.getUser().getName()) : "Ứng viên";
        String jobTitle = application.getJob() != null ? safeText(application.getJob().getTitle()) : "job";
        if (previousStatus != null && nextStatus != null) {
            return candidate + " - " + jobTitle + ": " + safeText(previousStatus) + " -> " + safeText(nextStatus);
        }
        return candidate + " - " + jobTitle + " đã được mở CV.";
    }

    private String buildMemberDetail(String prefix, Company company, User targetUser, String role) {
        StringBuilder builder = new StringBuilder(prefix);
        if (targetUser != null) {
            builder.append(" ").append(safeText(targetUser.getName()));
        }
        if (company != null) {
            builder.append(" vào ").append(safeText(company.getName()));
        }
        if (role != null && !role.isBlank()) {
            builder.append(" với quyền ").append(role.toUpperCase());
        }
        return builder.toString();
    }

    private String actionTitleFor(String actionType, String fallback) {
        if (actionType == null) {
            return fallback;
        }
        return switch (actionType) {
            case "JOB_OPENED" -> "Mở job";
            case "JOB_CLOSED" -> "Đóng job";
            case "JOB_ARCHIVED" -> "Archive job";
            default -> fallback;
        };
    }

    private String safeText(String value) {
        return value == null || value.isBlank() ? "Đang cập nhật" : value;
    }
}

