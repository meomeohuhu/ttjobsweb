package com.ttjobs.backend.controller;

import com.ttjobs.backend.dto.admin.AdminAuditLogDTO;
import com.ttjobs.backend.dto.admin.AdminAiMonitoringDTO;
import com.ttjobs.backend.dto.admin.AdminCompanyUpdateRequest;
import com.ttjobs.backend.dto.admin.AdminJobUpdateRequest;
import com.ttjobs.backend.dto.admin.AdminRoleUpdateRequest;
import com.ttjobs.backend.dto.admin.AdminStatsDTO;
import com.ttjobs.backend.dto.admin.AdminUserDTO;
import com.ttjobs.backend.dto.admin.AiMatchEventDTO;
import com.ttjobs.backend.dto.common.AdminActionRequest;
import com.ttjobs.backend.dto.company.CompanyDTO;
import com.ttjobs.backend.dto.company.CompanyVerificationDTO;
import com.ttjobs.backend.dto.job.JobDTO;
import com.ttjobs.backend.entity.Company;
import com.ttjobs.backend.service.AdminAuditLogService;
import com.ttjobs.backend.service.AdminService;
import com.ttjobs.backend.service.AiMonitoringService;
import com.ttjobs.backend.service.CompanyVerificationService;
import com.ttjobs.backend.service.SavedSearchService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.time.LocalDate;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private AdminService adminService;
    @Autowired(required = false)
    private CompanyVerificationService companyVerificationService;
    @Autowired(required = false)
    private AdminAuditLogService adminAuditLogService;
    @Autowired(required = false)
    private SavedSearchService savedSearchService;
    @Autowired(required = false)
    private AiMonitoringService aiMonitoringService;

    @GetMapping("/users")
    public List<AdminUserDTO> getUsers(@RequestParam(required = false) String role) {
        return adminService.getUsers(role);
    }

    @PutMapping("/users/{id}/role")
    public AdminUserDTO updateUserRole(@PathVariable Long id, @Valid @RequestBody AdminRoleUpdateRequest request) {
        return adminService.updateUserRole(id, request);
    }

    @DeleteMapping("/users/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable Long id) {
        adminService.deleteUser(id);
    }

    @GetMapping("/companies")
    public List<CompanyDTO> getCompanies() {
        return adminService.getCompanies();
    }

    @DeleteMapping("/companies/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCompany(@PathVariable Long id, @RequestBody(required = false) AdminActionRequest request) {
        adminService.deleteCompany(id, request);
    }

    @PutMapping("/companies/{id}")
    public CompanyDTO updateCompany(@PathVariable Long id, @RequestBody AdminCompanyUpdateRequest request) {
        return adminService.updateCompany(id, request);
    }

    @GetMapping("/jobs")
    public List<JobDTO> getJobs() {
        return adminService.getJobs();
    }

    @PutMapping("/jobs/{id}")
    public JobDTO updateJob(@PathVariable Long id, @RequestBody AdminJobUpdateRequest request) {
        return adminService.updateJob(id, request);
    }

    @DeleteMapping("/jobs/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteJob(@PathVariable Long id, @RequestBody(required = false) AdminActionRequest request) {
        adminService.deleteJob(id, request);
    }

    @GetMapping("/stats")
    public AdminStatsDTO getStats(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        LocalDateTime fromDateTime = from == null ? null : from.atStartOfDay();
        LocalDateTime toDateTime = to == null ? null : to.plusDays(1).atStartOfDay().minusNanos(1);
        return adminService.getStats(fromDateTime, toDateTime);
    }

    @GetMapping("/audit-logs")
    public List<AdminAuditLogDTO> getAuditLogs(@RequestParam(required = false) Integer size) {
        return adminAuditLogService.getRecent(size);
    }

    @GetMapping("/companies/verifications")
    public List<CompanyVerificationDTO> getCompanyVerifications(@RequestParam(required = false) String status) {
        return companyVerificationService.getAdminQueue(status);
    }

    @PostMapping("/companies/{companyId}/verify")
    public CompanyVerificationDTO verifyCompany(@PathVariable Long companyId, @RequestBody(required = false) AdminActionRequest request) {
        return companyVerificationService.review(companyId, Company.VerificationStatus.VERIFIED, request);
    }

    @PostMapping("/companies/{companyId}/reject")
    public CompanyVerificationDTO rejectCompany(@PathVariable Long companyId, @RequestBody(required = false) AdminActionRequest request) {
        return companyVerificationService.review(companyId, Company.VerificationStatus.REJECTED, request);
    }

    @PostMapping("/companies/{companyId}/suspend")
    public CompanyVerificationDTO suspendCompany(@PathVariable Long companyId, @RequestBody(required = false) AdminActionRequest request) {
        return companyVerificationService.review(companyId, Company.VerificationStatus.SUSPENDED, request);
    }

    @PostMapping("/job-alerts/run")
    public java.util.Map<String, Integer> runJobAlerts() {
        return java.util.Map.of("notifiedCount", savedSearchService.runAlerts());
    }

    @GetMapping("/ai/monitoring")
    public AdminAiMonitoringDTO getAiMonitoring() {
        return aiMonitoringService.getMonitoring();
    }

    @GetMapping("/ai/training-events")
    public List<AiMatchEventDTO> getAiTrainingEvents(
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) String eventType,
            @RequestParam(required = false) String label,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(required = false) Integer minScore,
            @RequestParam(required = false) Integer maxScore) {
        return aiMonitoringService.getTrainingEvents(
                size == null ? 100 : size,
                eventType,
                label,
                from,
                to,
                minScore,
                maxScore);
    }
}

