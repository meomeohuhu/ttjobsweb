package com.ttjobs.backend.service;

import com.ttjobs.backend.dto.job.JobDTO;
import com.ttjobs.backend.dto.savedsearch.SavedSearchDTO;
import com.ttjobs.backend.dto.savedsearch.SavedSearchRequest;
import com.ttjobs.backend.entity.Job;
import com.ttjobs.backend.entity.JobAlertHistory;
import com.ttjobs.backend.entity.SavedSearch;
import com.ttjobs.backend.entity.User;
import com.ttjobs.backend.repository.JobAlertHistoryRepository;
import com.ttjobs.backend.repository.JobRepository;
import com.ttjobs.backend.repository.JobSpecifications;
import com.ttjobs.backend.repository.SavedSearchRepository;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class SavedSearchService {

    @Autowired
    private SavedSearchRepository savedSearchRepository;
    @Autowired
    private JobRepository jobRepository;
    @Autowired
    private JobAlertHistoryRepository jobAlertHistoryRepository;
    @Autowired
    private AuthContextService authContextService;
    @Autowired
    private NotificationService notificationService;

    public List<SavedSearchDTO> getMine() {
        User currentUser = authContextService.requireCurrentUser();
        return savedSearchRepository.findByUserIdOrderByUpdatedAtDesc(currentUser.getId())
                .stream().map(this::toDto).toList();
    }

    @Transactional
    public SavedSearchDTO create(SavedSearchRequest request) {
        User currentUser = authContextService.requireCurrentUser();
        SavedSearch search = new SavedSearch();
        search.setUser(currentUser);
        apply(search, request);
        return toDto(savedSearchRepository.save(search));
    }

    @Transactional
    public SavedSearchDTO update(Long id, SavedSearchRequest request) {
        User currentUser = authContextService.requireCurrentUser();
        SavedSearch search = savedSearchRepository.findByIdAndUserId(id, currentUser.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Saved search not found"));
        apply(search, request);
        return toDto(savedSearchRepository.save(search));
    }

    @Transactional
    public void delete(Long id) {
        User currentUser = authContextService.requireCurrentUser();
        SavedSearch search = savedSearchRepository.findByIdAndUserId(id, currentUser.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Saved search not found"));
        savedSearchRepository.delete(search);
    }

    public List<JobDTO> run(Long id) {
        User currentUser = authContextService.requireCurrentUser();
        SavedSearch search = savedSearchRepository.findByIdAndUserId(id, currentUser.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Saved search not found"));
        return findJobs(search).stream().map(this::toJobDto).toList();
    }

    @Transactional
    public int runAlerts() {
        int notified = 0;
        for (SavedSearch search : savedSearchRepository.findByActiveTrueOrderByUpdatedAtDesc()) {
            if (search.getUser() == null) {
                continue;
            }
            for (Job job : findJobs(search).stream().limit(5).toList()) {
                if (job.getId() == null || jobAlertHistoryRepository.existsByUserIdAndSavedSearchIdAndJobId(
                        search.getUser().getId(), search.getId(), job.getId())) {
                    continue;
                }
                JobAlertHistory history = new JobAlertHistory();
                history.setUserId(search.getUser().getId());
                history.setJobId(job.getId());
                history.setSavedSearchId(search.getId());
                history.setSentAt(java.time.LocalDateTime.now());
                jobAlertHistoryRepository.save(history);
                notificationService.createNotification(
                        search.getUser(),
                        "Việc làm phù hợp với tìm kiếm đã lưu",
                        job.getTitle(),
                        "JOB_ALERT",
                        "/jobs?keyword=" + Objects.toString(search.getKeyword(), "")
                );
                notified++;
            }
        }
        return notified;
    }

    private List<Job> findJobs(SavedSearch search) {
        Specification<Job> spec = Specification.where(JobSpecifications.activeJobs())
                .and(JobSpecifications.statusEquals("open"))
                .and(JobSpecifications.companyVerified());
        if (hasText(search.getKeyword())) spec = spec.and(JobSpecifications.keywordLike(search.getKeyword()));
        if (hasText(search.getLocation())) spec = spec.and(JobSpecifications.locationLike(search.getLocation()));
        if (hasText(search.getCategory())) spec = spec.and(JobSpecifications.categoryIn(List.of(search.getCategory())));
        if (hasText(search.getJobType())) spec = spec.and(JobSpecifications.jobTypeEquals(search.getJobType()));
        if (hasText(search.getExperienceLevel())) spec = spec.and(JobSpecifications.experienceLevelEquals(search.getExperienceLevel()));
        if (search.getSalaryMin() != null) spec = spec.and(JobSpecifications.salaryMinGte(search.getSalaryMin()));
        if (search.getSalaryMax() != null) spec = spec.and(JobSpecifications.salaryMaxLte(search.getSalaryMax()));
        if (Boolean.TRUE.equals(search.getRemoteOnly())) spec = spec.and(JobSpecifications.remoteFriendly());
        List<String> skills = split(search.getSkills());
        if (!skills.isEmpty()) spec = spec.and(JobSpecifications.hasAnySkill(skills));
        return jobRepository.findAll(spec, PageRequest.of(0, 30, Sort.by(Sort.Direction.DESC, "postedDate"))).getContent();
    }

    private void apply(SavedSearch search, SavedSearchRequest request) {
        if (request == null || !hasText(request.getName())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "name is required");
        }
        search.setName(request.getName().trim());
        search.setKeyword(trim(request.getKeyword()));
        search.setLocation(trim(request.getLocation()));
        search.setCategory(trim(request.getCategory()));
        search.setJobType(trim(request.getJobType()));
        search.setExperienceLevel(trim(request.getExperienceLevel()));
        search.setSalaryMin(request.getSalaryMin());
        search.setSalaryMax(request.getSalaryMax());
        search.setRemoteOnly(request.getRemoteOnly());
        search.setSkills(String.join("\n", Objects.requireNonNullElse(request.getSkills(), List.of())));
        search.setAlertFrequency(hasText(request.getAlertFrequency()) ? request.getAlertFrequency().trim().toUpperCase() : "DAILY");
        search.setActive(request.getActive() == null || request.getActive());
    }

    private SavedSearchDTO toDto(SavedSearch search) {
        SavedSearchDTO dto = new SavedSearchDTO();
        dto.setId(search.getId());
        dto.setName(search.getName());
        dto.setKeyword(search.getKeyword());
        dto.setLocation(search.getLocation());
        dto.setCategory(search.getCategory());
        dto.setJobType(search.getJobType());
        dto.setExperienceLevel(search.getExperienceLevel());
        dto.setSalaryMin(search.getSalaryMin());
        dto.setSalaryMax(search.getSalaryMax());
        dto.setRemoteOnly(search.getRemoteOnly());
        dto.setSkills(split(search.getSkills()));
        dto.setAlertFrequency(search.getAlertFrequency());
        dto.setActive(search.getActive());
        dto.setCreatedAt(search.getCreatedAt());
        dto.setUpdatedAt(search.getUpdatedAt());
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

    private List<String> split(String raw) {
        if (raw == null || raw.isBlank()) return List.of();
        return Arrays.stream(raw.split("\\R")).map(String::trim).filter(value -> !value.isBlank()).toList();
    }
    private boolean hasText(String value) { return value != null && !value.isBlank(); }
    private String trim(String value) { return value == null ? null : value.trim(); }
}
