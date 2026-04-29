package com.ttjobs.backend.service;

import com.ttjobs.backend.dto.JobApplicationDTO;
import com.ttjobs.backend.dto.RecruiterActivityLogDTO;
import com.ttjobs.backend.dto.RecruiterDashboardCompanyDTO;
import com.ttjobs.backend.dto.RecruiterDashboardDTO;
import com.ttjobs.backend.dto.RecruiterDashboardJobDTO;
import com.ttjobs.backend.entity.Company;
import com.ttjobs.backend.entity.CompanyMember;
import com.ttjobs.backend.entity.Job;
import com.ttjobs.backend.entity.JobApplication;
import com.ttjobs.backend.entity.User;
import com.ttjobs.backend.repository.CompanyMemberRepository;
import com.ttjobs.backend.repository.JobApplicationRepository;
import com.ttjobs.backend.repository.JobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class RecruiterDashboardService {

    private static final List<String> APPLICATION_STATUSES = List.of(
            "submitted", "reviewing", "shortlisted", "interviewed", "offered", "hired", "rejected", "withdrawn"
    );

    @Autowired
    private AuthContextService authContextService;

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private JobApplicationRepository jobApplicationRepository;

    @Autowired
    private CompanyMemberRepository companyMemberRepository;

    @Autowired
    private RecruiterActivityLogService recruiterActivityLogService;

    public RecruiterDashboardDTO getDashboard() {
        User currentUser = authContextService.requireCurrentUser();
        if (currentUser.getRole() != User.Role.RECRUITER && !authContextService.isAdmin(currentUser)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only recruiter can access dashboard");
        }

        List<Job> managedJobs = loadManagedJobs(currentUser);
        List<JobApplication> managedApplications = loadManagedApplications(managedJobs, currentUser);

        RecruiterDashboardDTO dto = new RecruiterDashboardDTO();
        dto.setOpenJobCount(countOpenJobs(managedJobs));
        dto.setNewApplicationCount(countRecentApplications(managedApplications, 7));
        dto.setExpiringSoonJobCount(countExpiringSoonJobs(managedJobs, 14));
        dto.setApplicationStatusCounts(countApplicationsByStatus(managedApplications));
        dto.setExpiringSoonJobs(mapExpiringSoonJobs(managedJobs, managedApplications, 14));
        dto.setRecentApplications(mapRecentApplications(managedApplications, 6));
        dto.setManagedCompanies(mapManagedCompanies(managedJobs, currentUser));
        dto.setRecentActivities(recruiterActivityLogService.getRecentActivities(8));
        return dto;
    }

    public List<RecruiterActivityLogDTO> getRecentActivities(int limit) {
        return recruiterActivityLogService.getRecentActivities(limit);
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

    private List<JobApplication> loadManagedApplications(List<Job> managedJobs, User currentUser) {
        if (managedJobs.isEmpty()) {
            return List.of();
        }

        if (authContextService.isAdmin(currentUser)) {
            return jobApplicationRepository.findAll().stream()
                    .filter(application -> application.getJob() != null)
                    .filter(application -> application.getJob().getDeletedAt() == null)
                    .filter(application -> application.getJob().getCompany() != null
                            && application.getJob().getCompany().getDeletedAt() == null)
                    .toList();
        }

        List<Long> jobIds = managedJobs.stream()
                .map(Job::getId)
                .filter(Objects::nonNull)
                .toList();

        return jobApplicationRepository.findByJobIdIn(jobIds);
    }

    private Long countOpenJobs(List<Job> jobs) {
        return jobs.stream()
                .filter(job -> "open".equalsIgnoreCase(job.getStatus()))
                .count();
    }

    private Long countRecentApplications(List<JobApplication> applications, int days) {
        LocalDateTime threshold = LocalDate.now().minusDays(days).atStartOfDay();
        return applications.stream()
                .filter(application -> application.getApplicationDate() != null)
                .filter(application -> !application.getApplicationDate().isBefore(threshold))
                .count();
    }

    private Long countExpiringSoonJobs(List<Job> jobs, int days) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime threshold = now.plusDays(days);
        return jobs.stream()
                .filter(job -> "open".equalsIgnoreCase(job.getStatus()))
                .filter(job -> job.getApplicationDeadline() != null)
                .filter(job -> !job.getApplicationDeadline().isBefore(now))
                .filter(job -> !job.getApplicationDeadline().isAfter(threshold))
                .count();
    }

    private Map<String, Long> countApplicationsByStatus(List<JobApplication> applications) {
        Map<String, Long> counts = new LinkedHashMap<>();
        for (String status : APPLICATION_STATUSES) {
            counts.put(status, 0L);
        }

        applications.stream()
                .map(JobApplication::getStatus)
                .filter(Objects::nonNull)
                .map(status -> status.toLowerCase())
                .forEach(status -> counts.merge(status, 1L, Long::sum));

        return counts;
    }

    private List<RecruiterDashboardJobDTO> mapExpiringSoonJobs(List<Job> jobs, List<JobApplication> applications, int days) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime threshold = now.plusDays(days);
        Map<Long, Long> applicationCountByJobId = applications.stream()
                .filter(application -> application.getJob() != null)
                .collect(Collectors.groupingBy(
                        application -> application.getJob().getId(),
                        Collectors.counting()
                ));

        return jobs.stream()
                .filter(job -> "open".equalsIgnoreCase(job.getStatus()))
                .filter(job -> job.getApplicationDeadline() != null)
                .filter(job -> !job.getApplicationDeadline().isBefore(now))
                .filter(job -> !job.getApplicationDeadline().isAfter(threshold))
                .sorted(Comparator.comparing(Job::getApplicationDeadline))
                .map(job -> {
                    RecruiterDashboardJobDTO dto = new RecruiterDashboardJobDTO();
                    dto.setId(job.getId());
                    dto.setTitle(job.getTitle());
                    dto.setLocation(job.getLocation());
                    dto.setStatus(job.getStatus());
                    if (job.getCompany() != null) {
                        dto.setCompanyName(job.getCompany().getName());
                        dto.setCompanyLogoUrl(job.getCompany().getLogoUrl());
                    }
                    dto.setApplicationDeadline(job.getApplicationDeadline());
                    dto.setApplicationCount(applicationCountByJobId.getOrDefault(job.getId(), 0L));
                    dto.setDaysUntilDeadline(ChronoUnit.DAYS.between(now.toLocalDate(), job.getApplicationDeadline().toLocalDate()));
                    return dto;
                })
                .limit(5)
                .toList();
    }

    private List<JobApplicationDTO> mapRecentApplications(List<JobApplication> applications, int limit) {
        return applications.stream()
                .sorted(Comparator.comparing(JobApplication::getApplicationDate, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(limit)
                .map(this::toDto)
                .toList();
    }

    private List<RecruiterDashboardCompanyDTO> mapManagedCompanies(List<Job> jobs, User currentUser) {
        Map<Long, List<Job>> jobsByCompany = jobs.stream()
                .filter(job -> job.getCompany() != null && job.getCompany().getId() != null)
                .collect(Collectors.groupingBy(job -> job.getCompany().getId()));

        return jobsByCompany.entrySet().stream()
                .map(entry -> {
                    Job sampleJob = entry.getValue().stream().findFirst().orElse(null);
                    Company company = sampleJob != null ? sampleJob.getCompany() : null;
                    RecruiterDashboardCompanyDTO dto = new RecruiterDashboardCompanyDTO();
                    dto.setCompanyId(entry.getKey());
                    dto.setCompanyName(company != null ? company.getName() : null);
                    dto.setCompanyLogoUrl(company != null ? company.getLogoUrl() : null);
                    dto.setJobCount((long) entry.getValue().size());
                    dto.setMemberCount(company != null
                            ? companyMemberRepository.findByCompanyId(company.getId()).size()
                            : 0L);
                    return dto;
                })
                .sorted(Comparator.comparing(RecruiterDashboardCompanyDTO::getCompanyName, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)))
                .toList();
    }

    private JobApplicationDTO toDto(JobApplication application) {
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
        dto.setHasCv(application.getCvUrl() != null || application.getCv() != null);
        return dto;
    }
}
