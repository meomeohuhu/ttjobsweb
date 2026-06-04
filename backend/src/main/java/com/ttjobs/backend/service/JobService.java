package com.ttjobs.backend.service;

import com.ttjobs.backend.dto.job.JobCategoryStatDTO;
import com.ttjobs.backend.dto.job.JobDTO;
import com.ttjobs.backend.entity.Company;
import com.ttjobs.backend.entity.Job;
import com.ttjobs.backend.entity.User;
import com.ttjobs.backend.repository.CompanyRepository;
import com.ttjobs.backend.repository.JobRepository;
import com.ttjobs.backend.repository.JobSpecifications;
import com.ttjobs.backend.repository.JobWithSavedCount;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Objects;
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

    @Autowired
    private RecruiterActivityLogService recruiterActivityLogService;
    @Autowired
    private ImageUploadService imageUploadService;
    @Autowired
    private CompanyFollowService companyFollowService;
    @Autowired
    private CompanyVerificationStatusService companyVerificationStatusService;

    public List<JobDTO> getAllJobs() {
        // Default candidate-facing list only shows open jobs.
        Pageable pageable = PageRequest.of(0, 50);
        return jobRepository.findJobsWithSavedCount(OPEN, Company.VerificationStatus.VERIFIED, pageable)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<JobDTO> getPublicJobs(String keyword, String category, String location, String companyName,
                                      String jobType, String experienceLevel, BigDecimal salaryMin,
                                      BigDecimal salaryMax, String sort, Integer page, Integer size) {
        boolean hasFilters = hasText(keyword) || hasText(category) || hasText(location) || hasText(companyName)
                || hasText(jobType) || hasText(experienceLevel) || salaryMin != null || salaryMax != null
                || hasText(sort) || page != null || size != null;
        if (!hasFilters) {
            return getAllJobs();
        }
        return searchJobs(keyword, category, location, companyName, jobType, experienceLevel, OPEN,
                salaryMin, salaryMax, null, sort, page, size);
    }

    public List<JobDTO> getHighlightedJobs(String type, Integer size) {
        String normalizedType = (type == null || type.isBlank()) ? "high_salary" : type.trim().toLowerCase();
        if (!"high_salary".equals(normalizedType)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid highlight type");
        }

        int safeSize = size == null ? 12 : Math.max(1, Math.min(size, 50));
        Pageable pageable = PageRequest.of(0, safeSize);
        return jobRepository.findHighlightedJobs(OPEN, Company.VerificationStatus.VERIFIED, pageable)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<JobDTO> getBestJobs(String type, Integer size) {
        String normalizedType = (type == null || type.isBlank()) ? "most_saved" : type.trim().toLowerCase();
        if (!"most_saved".equals(normalizedType)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid best job type");
        }

        int safeSize = size == null ? 12 : Math.max(1, Math.min(size, 50));
        Pageable pageable = PageRequest.of(0, safeSize);
        return jobRepository.findBestJobs(OPEN, Company.VerificationStatus.VERIFIED, pageable)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<JobCategoryStatDTO> getTopCategories(Integer size) {
        int safeSize = size == null ? 8 : Math.max(1, Math.min(size, 24));
        Pageable pageable = PageRequest.of(0, safeSize);
        return jobRepository.findTopCategories(OPEN, Company.VerificationStatus.VERIFIED, pageable)
                .stream()
                .map(item -> {
                    String category = normalizeCategoryCode(item.getCategory());
                    return new JobCategoryStatDTO(category, resolveCategoryLabel(category), item.getJobCount());
                })
                .collect(Collectors.toList());
    }

    public Optional<JobDTO> getJobById(Long id) {
        Job job = jobRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Job not found"));

        User currentUser = authContextService.getCurrentUserOptional().orElse(null);
        boolean publicVisible = OPEN.equals(job.getStatus())
                && job.getCompany() != null
                && companyVerificationStatusService.isVerified(job.getCompany());
        if (!publicVisible) {
            if (currentUser == null || !canManageJob(currentUser, job)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You cannot access this job");
            }
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
        if (OPEN.equals(job.getStatus()) && !companyVerificationStatusService.isVerified(company)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Company must be verified before publishing jobs");
        }
        normalizeCompensation(job);
        Job saved = jobRepository.save(job);
        recruiterActivityLogService.logJobCreated(currentUser, saved);
        companyFollowService.notifyFollowersAboutNewJob(saved);
        return convertToDTO(saved);
    }

    public JobDTO updateJob(Long id, Job jobDetails) {
        User currentUser = authContextService.requireCurrentUser();

        Job job = jobRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Job not found"));

        String previousStatus = job.getStatus();
        boolean changed = false;

        requireCompanyOwnership(currentUser, job.getCompany());

        if (jobDetails.getTitle() != null) {
            job.setTitle(jobDetails.getTitle());
            changed = true;
        }
        if (jobDetails.getDescription() != null) {
            job.setDescription(jobDetails.getDescription());
            changed = true;
        }
        if (jobDetails.getLocation() != null) {
            job.setLocation(jobDetails.getLocation());
            changed = true;
        }
        if (jobDetails.getSalary() != null) {
            job.setSalary(jobDetails.getSalary());
            changed = true;
        }
        if (jobDetails.getSalaryMin() != null) {
            job.setSalaryMin(jobDetails.getSalaryMin());
            changed = true;
        }
        if (jobDetails.getSalaryMax() != null) {
            job.setSalaryMax(jobDetails.getSalaryMax());
            changed = true;
        }
        if (jobDetails.getCurrency() != null && !jobDetails.getCurrency().isBlank()) {
            job.setCurrency(jobDetails.getCurrency().trim().toUpperCase());
            changed = true;
        }
        if (jobDetails.getJobType() != null) {
            job.setJobType(jobDetails.getJobType());
            changed = true;
        }
        if (jobDetails.getExperienceLevel() != null) {
            job.setExperienceLevel(jobDetails.getExperienceLevel());
            changed = true;
        }
        // Update job category if provided.
        if (jobDetails.getCategory() != null) {
            job.setCategory(jobDetails.getCategory());
            changed = true;
        }
        if (jobDetails.getImageUrl() != null) {
            job.setImageUrl(jobDetails.getImageUrl());
            changed = true;
        }
        if (jobDetails.getApplicationDeadline() != null) {
            job.setApplicationDeadline(jobDetails.getApplicationDeadline());
            changed = true;
        }

        if (jobDetails.getStatus() != null && !jobDetails.getStatus().isBlank()) {
            String nextStatus = normalizeJobStatus(jobDetails.getStatus(), null);
            if (OPEN.equals(nextStatus)
                    && job.getCompany() != null
                    && !companyVerificationStatusService.isVerified(job.getCompany())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Company must be verified before publishing jobs");
            }
            validateJobTransition(job.getStatus(), nextStatus);
            job.setStatus(nextStatus);
            changed = true;
        }
        normalizeCompensation(job);
        Job saved = jobRepository.save(job);
        if (!Objects.equals(previousStatus, saved.getStatus())) {
            recruiterActivityLogService.logJobStatusChanged(
                    currentUser,
                    saved,
                    resolveJobStatusAction(saved.getStatus()),
                    previousStatus,
                    saved.getStatus()
            );
            companyFollowService.notifyFollowersAboutNewJob(saved);
        } else if (changed) {
            recruiterActivityLogService.logJobUpdated(currentUser, saved);
        }
        return convertToDTO(saved);
    }

    public void deleteJob(Long id) {
        // Keep API contract but perform soft-close instead of physical delete.
        User currentUser = authContextService.requireCurrentUser();

        Job job = jobRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Job not found"));

        requireCompanyOwnership(currentUser, job.getCompany());
        String previousStatus = job.getStatus();
        validateJobTransition(job.getStatus(), ARCHIVED);
        job.setStatus(ARCHIVED);
        job.setDeletedAt(LocalDateTime.now());
        Job saved = jobRepository.save(job);
        recruiterActivityLogService.logJobStatusChanged(currentUser, saved, "JOB_ARCHIVED", previousStatus, ARCHIVED);
    }

    public JobDTO uploadJobImage(Long id, org.springframework.web.multipart.MultipartFile file) {
        User currentUser = authContextService.requireCurrentUser();
        Job job = jobRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Job not found"));

        requireCompanyOwnership(currentUser, job.getCompany());
        String imageUrl = imageUploadService.uploadImage(file, "ttjobs/jobs", "job-" + job.getId());
        job.setImageUrl(imageUrl);
        Job saved = jobRepository.save(job);
        recruiterActivityLogService.logJobUpdated(currentUser, saved);
        return convertToDTO(saved);
    }

    public List<JobDTO> searchJobs(String title, String location, String companyName,
                                   String jobType, String experienceLevel) {
        return searchJobs(title, location, companyName, jobType, experienceLevel, OPEN, 0, 50);
    }

    public List<JobDTO> searchJobs(String title, String location, String companyName,
                                   String jobType, String experienceLevel, String status,
                                   Integer page, Integer size) {
        return searchJobs(title, location, companyName, jobType, experienceLevel, status,
                null, null, null, page, size);
    }

    public List<JobDTO> searchJobs(String keyword, String location, String companyName,
                                   String jobType, String experienceLevel, String status,
                                   BigDecimal salaryMin, BigDecimal salaryMax, List<String> skills,
                                   Integer page, Integer size) {
        return searchJobs(keyword, null, location, companyName, jobType, experienceLevel, status,
                salaryMin, salaryMax, skills, "latest", page, size);
    }

    public List<JobDTO> searchJobs(String keyword, String category, String location, String companyName,
                                   String jobType, String experienceLevel, String status,
                                   BigDecimal salaryMin, BigDecimal salaryMax, List<String> skills,
                                   String sort, Integer page, Integer size) {
        String normalizedStatus = normalizeJobStatus(status, OPEN);
        int safePage = page == null ? 0 : Math.max(page, 0);
        int safeSize = size == null ? 20 : Math.max(1, Math.min(size, 100));
        Pageable pageable = PageRequest.of(safePage, safeSize, resolveSort(sort));

        Specification<Job> spec = Specification.where(JobSpecifications.activeJobs());
        if (OPEN.equals(normalizedStatus)) {
            spec = spec.and(JobSpecifications.companyVerified());
        }
        if (keyword != null && !keyword.isBlank()) {
            spec = spec.and(JobSpecifications.keywordLike(keyword));
        }
        if (location != null && !location.isBlank()) {
            spec = spec.and(JobSpecifications.locationLike(location));
        }
        if (category != null && !category.isBlank()) {
            spec = spec.and(JobSpecifications.categoryIn(List.of(category)));
        }
        if (companyName != null && !companyName.isBlank()) {
            spec = spec.and(JobSpecifications.companyNameLike(companyName));
        }
        if (jobType != null && !jobType.isBlank()) {
            spec = spec.and(JobSpecifications.jobTypeEquals(jobType));
        }
        if (experienceLevel != null && !experienceLevel.isBlank()) {
            spec = spec.and(JobSpecifications.experienceLevelEquals(experienceLevel));
        }
        if (normalizedStatus != null && !normalizedStatus.isBlank()) {
            spec = spec.and(JobSpecifications.statusEquals(normalizedStatus));
        }
        if (salaryMin != null) {
            spec = spec.and(JobSpecifications.salaryMinGte(salaryMin));
        }
        if (salaryMax != null) {
            spec = spec.and(JobSpecifications.salaryMaxLte(salaryMax));
        }
        if (skills != null && !skills.isEmpty()) {
            spec = spec.and(JobSpecifications.hasAnySkill(skills));
        }

        return jobRepository.findAll(spec, pageable)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private Sort resolveSort(String sort) {
        String normalized = sort == null ? "latest" : sort.trim().toLowerCase();
        return switch (normalized) {
            case "salary_high", "salary_desc" -> Sort.by(
                    Sort.Order.desc("salaryMax"),
                    Sort.Order.desc("salary"),
                    Sort.Order.desc("salaryMin"),
                    Sort.Order.desc("postedDate")
            );
            case "salary_low", "salary_asc" -> Sort.by(
                    Sort.Order.asc("salaryMin"),
                    Sort.Order.asc("salary"),
                    Sort.Order.asc("salaryMax"),
                    Sort.Order.desc("postedDate")
            );
            default -> Sort.by(Sort.Order.desc("postedDate"), Sort.Order.desc("id"));
        };
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
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

    private JobDTO convertToDTO(JobWithSavedCount projection) {
        JobDTO dto = convertToDTO(projection.getJob());
        dto.setSavedCount(projection.getSavedCount());
        return dto;
    }

    private String resolveJobStatusAction(String status) {
        if (status == null) {
            return "JOB_UPDATED";
        }
        return switch (status.toLowerCase()) {
            case OPEN -> "JOB_OPENED";
            case CLOSED -> "JOB_CLOSED";
            case ARCHIVED -> "JOB_ARCHIVED";
            default -> "JOB_UPDATED";
        };
    }

    private String normalizeCategoryCode(String category) {
        if (category == null || category.isBlank()) {
            return "OTHER";
        }
        return category.trim().toUpperCase();
    }

    private String resolveCategoryLabel(String category) {
        return switch (category) {
            case "SALES" -> "Kinh doanh - Bán hàng";
            case "MARKETING" -> "Marketing - PR - Quảng cáo";
            case "CUSTOMER-SERVICE" -> "Chăm sóc khách hàng";
            case "HR" -> "Nhân sự - Hành chính";
            case "INFORMATION-TECHNOLOGY" -> "Công nghệ Thông tin";
            case "FINANCE" -> "Tài chính - Ngân hàng";
            case "REAL-ESTATE" -> "Bất động sản";
            case "ACCOUNTING" -> "Kế toán - Kiểm toán - Thuế";
            case "DESIGN" -> "Thiết kế";
            default -> "Ngành nghề khác";
        };
    }
}

