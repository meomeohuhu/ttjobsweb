package com.ttjobs.backend.service;

import com.ttjobs.backend.dto.JobDTO;
import com.ttjobs.backend.entity.Company;
import com.ttjobs.backend.entity.Job;
import com.ttjobs.backend.entity.User;
import com.ttjobs.backend.repository.CompanyRepository;
import com.ttjobs.backend.repository.JobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class JobService {

    private static final String DRAFT = "draft";
    private static final String OPEN = "open";
    private static final String CLOSED = "closed";
    private static final String ARCHIVED = "archived";
    private static final Set<String> VALID_STATUS = Set.of(DRAFT, OPEN, CLOSED, ARCHIVED);

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private CompanyAuthorizationService companyAuthorizationService;

    @Autowired
    private AuthContextService authContextService;

    public List<JobDTO> getAllJobs() {
        // Default candidate-facing list only shows open jobs.
        return searchJobs(null, null, null, null, null, OPEN, 0, 50);
    }

    public Optional<JobDTO> getJobById(Long id) {
        Job job = jobRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Job not found"));

        User currentUser = authContextService.requireCurrentUser();
        if (!OPEN.equals(job.getStatus()) && !canManageJob(currentUser, job)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You cannot access this job");
        }

        return Optional.of(convertToDTO(job));
    }

    public JobDTO createJob(Job job) {
        User currentUser = authContextService.requireCurrentUser();
        requireRecruiterOrAdmin(currentUser);

        if (job.getCompany() == null || job.getCompany().getId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Company is required");
        }

        Company company = companyRepository.findByIdAndDeletedAtIsNull(job.getCompany().getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Company not found"));

        requireCompanyOwnership(currentUser, company);

        job.setCompany(company);
        job.setPostedDate(LocalDateTime.now());
        job.setStatus(normalizeJobStatus(job.getStatus(), DRAFT));
        normalizeCompensation(job);

        return convertToDTO(jobRepository.save(job));
    }

    public JobDTO updateJob(Long id, Job jobDetails) {
        User currentUser = authContextService.requireCurrentUser();

        Job job = jobRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Job not found"));

        requireCompanyOwnership(currentUser, job.getCompany());

        if (jobDetails.getTitle() != null) {
            job.setTitle(jobDetails.getTitle());
        }
        if (jobDetails.getDescription() != null) {
            job.setDescription(jobDetails.getDescription());
        }
        if (jobDetails.getLocation() != null) {
            job.setLocation(jobDetails.getLocation());
        }
        if (jobDetails.getSalary() != null) {
            job.setSalary(jobDetails.getSalary());
        }
        if (jobDetails.getSalaryMin() != null) {
            job.setSalaryMin(jobDetails.getSalaryMin());
        }
        if (jobDetails.getSalaryMax() != null) {
            job.setSalaryMax(jobDetails.getSalaryMax());
        }
        if (jobDetails.getCurrency() != null && !jobDetails.getCurrency().isBlank()) {
            job.setCurrency(jobDetails.getCurrency().trim().toUpperCase());
        }
        if (jobDetails.getJobType() != null) {
            job.setJobType(jobDetails.getJobType());
        }
        if (jobDetails.getExperienceLevel() != null) {
            job.setExperienceLevel(jobDetails.getExperienceLevel());
        }
        if (jobDetails.getApplicationDeadline() != null) {
            job.setApplicationDeadline(jobDetails.getApplicationDeadline());
        }

        if (jobDetails.getStatus() != null && !jobDetails.getStatus().isBlank()) {
            String nextStatus = normalizeJobStatus(jobDetails.getStatus(), null);
            validateJobTransition(job.getStatus(), nextStatus);
            job.setStatus(nextStatus);
        }
        normalizeCompensation(job);

        return convertToDTO(jobRepository.save(job));
    }

    public void deleteJob(Long id) {
        // Keep API contract but perform soft-close instead of physical delete.
        User currentUser = authContextService.requireCurrentUser();

        Job job = jobRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Job not found"));

        requireCompanyOwnership(currentUser, job.getCompany());
        validateJobTransition(job.getStatus(), ARCHIVED);
        job.setStatus(ARCHIVED);
        job.setDeletedAt(LocalDateTime.now());
        jobRepository.save(job);
    }

    public List<JobDTO> searchJobs(String title, String location, String companyName,
                                   String jobType, String experienceLevel) {
        return searchJobs(title, location, companyName, jobType, experienceLevel, OPEN, 0, 50);
    }

    public List<JobDTO> searchJobs(String title, String location, String companyName,
                                   String jobType, String experienceLevel, String status,
                                   Integer page, Integer size) {
        String normalizedStatus = normalizeJobStatus(status, OPEN);
        int safePage = page == null ? 0 : Math.max(page, 0);
        int safeSize = size == null ? 20 : Math.max(size, 1);
        Pageable pageable = PageRequest.of(safePage, safeSize);

        return jobRepository.findJobsWithFilters(title, location, companyName, jobType, experienceLevel,
                        normalizedStatus, pageable)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // Recruiter can list jobs of a specific owned company, admin can list any company jobs.
    public List<JobDTO> getCompanyJobs(Long companyId) {
        User currentUser = authContextService.requireCurrentUser();
        Company company = companyRepository.findByIdAndDeletedAtIsNull(companyId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Company not found"));

        requireCompanyOwnership(currentUser, company);
        return jobRepository.findByCompanyIdAndDeletedAtIsNull(companyId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private void normalizeCompensation(Job job) {
        if (job.getCurrency() == null || job.getCurrency().isBlank()) {
            job.setCurrency("VND");
        }

        if (job.getSalary() != null) {
            if (job.getSalaryMin() == null) {
                job.setSalaryMin(job.getSalary());
            }
            if (job.getSalaryMax() == null) {
                job.setSalaryMax(job.getSalary());
            }
        }

        if (job.getSalaryMin() != null && job.getSalaryMax() != null
                && job.getSalaryMin().compareTo(job.getSalaryMax()) > 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "salaryMin cannot be greater than salaryMax");
        }
    }

    private String normalizeJobStatus(String status, String fallback) {
        String value = (status == null || status.isBlank()) ? fallback : status.trim().toLowerCase();
        if (value == null || !VALID_STATUS.contains(value)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid job status");
        }
        return value;
    }

    private void validateJobTransition(String current, String next) {
        if (current == null) {
            return;
        }

        String from = current.toLowerCase();
        if (from.equals(next)) {
            return;
        }

        boolean valid = switch (from) {
            case DRAFT -> next.equals(OPEN) || next.equals(ARCHIVED);
            case OPEN -> next.equals(CLOSED) || next.equals(ARCHIVED);
            case CLOSED -> next.equals(ARCHIVED);
            case ARCHIVED -> false;
            default -> false;
        };

        if (!valid) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid job status transition");
        }
    }

    private void requireRecruiterOrAdmin(User user) {
        if (authContextService.isAdmin(user)) {
            return;
        }
        if (user.getRole() != User.Role.RECRUITER) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only recruiter can manage jobs");
        }
    }

    private boolean canManageJob(User user, Job job) {
        return user.getRole() == User.Role.RECRUITER
                && job.getCompany() != null
                && companyAuthorizationService.canManageCompany(user, job.getCompany());
    }

    private void requireCompanyOwnership(User user, Company company) {
        if (authContextService.isAdmin(user)) {
            return;
        }
        if (user.getRole() != User.Role.RECRUITER) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only recruiter can manage jobs");
        }
        companyAuthorizationService.requireManageCompany(user, company);
    }

    private JobDTO convertToDTO(Job job) {
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
        dto.setStatus(job.getStatus());
        dto.setPostedDate(job.getPostedDate());
        dto.setApplicationDeadline(job.getApplicationDeadline());
        if (job.getCompany() != null) {
            dto.setCompanyId(job.getCompany().getId());
            dto.setCompanyName(job.getCompany().getName());
        }
        return dto;
    }
}
