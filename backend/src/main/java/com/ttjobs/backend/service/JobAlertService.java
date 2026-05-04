package com.ttjobs.backend.service;

import com.ttjobs.backend.entity.Job;
import com.ttjobs.backend.entity.JobAlertHistory;
import com.ttjobs.backend.entity.JobNeedPreference;
import com.ttjobs.backend.entity.NotificationPreference;
import com.ttjobs.backend.entity.User;
import com.ttjobs.backend.repository.JobAlertHistoryRepository;
import com.ttjobs.backend.repository.JobNeedPreferenceRepository;
import com.ttjobs.backend.repository.JobRepository;
import com.ttjobs.backend.repository.JobSpecifications;
import com.ttjobs.backend.repository.NotificationPreferenceRepository;
import com.ttjobs.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
public class JobAlertService {

    private static final int MAX_JOBS_PER_EMAIL = 5;

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private JobNeedPreferenceRepository jobNeedPreferenceRepository;
    @Autowired
    private NotificationPreferenceRepository notificationPreferenceRepository;
    @Autowired
    private JobRepository jobRepository;
    @Autowired
    private JobAlertHistoryRepository jobAlertHistoryRepository;
    @Autowired
    private JobNeedPreferenceService jobNeedPreferenceService;
    @Autowired
    private EmailService emailService;

    @Value("${ttjobs.job-alert.lookback-hours:24}")
    private int lookbackHours;
    @Value("${ttjobs.email.enabled:false}")
    private boolean emailEnabled;

    @Transactional
    public int processAllAlerts() {
        if (!emailEnabled) {
            return 0;
        }
        int sentEmails = 0;
        for (User user : userRepository.findAllByRole(User.Role.CANDIDATE)) {
            if (!isEligibleForEmailAlerts(user)) {
                continue;
            }
            JobNeedPreference preference = jobNeedPreferenceRepository.findById(user.getId()).orElse(null);
            if (!jobNeedPreferenceService.hasConfiguredCriteria(preference)) {
                continue;
            }

            List<Job> jobs = findMatchingJobsForUser(user, preference);
            if (jobs.isEmpty()) {
                continue;
            }

            emailService.sendJobAlertEmail(user, jobs);
            saveAlertHistory(user, jobs);
            sentEmails++;
        }
        return sentEmails;
    }

    public List<Job> findMatchingJobsForUser(User user, JobNeedPreference preference) {
        if (user == null || preference == null) {
            return List.of();
        }

        LocalDateTime cutoff = LocalDateTime.now().minusHours(Math.max(1, lookbackHours));
        Specification<Job> spec = Specification.where(JobSpecifications.activeJobs())
                .and(JobSpecifications.statusEquals("open"))
                .and(JobSpecifications.postedSince(cutoff));

        if (hasText(preference.getDesiredTitle())) {
            spec = spec.and(JobSpecifications.keywordLike(preference.getDesiredTitle()));
        }
        if (hasText(preference.getDesiredLocation())) {
            spec = spec.and(JobSpecifications.locationLike(preference.getDesiredLocation()));
        }
        if (hasText(preference.getDesiredCategory())) {
            spec = spec.and(JobSpecifications.categoryIn(List.of(preference.getDesiredCategory())));
        }
        if (hasText(preference.getDesiredJobType())) {
            spec = spec.and(JobSpecifications.jobTypeEquals(preference.getDesiredJobType()));
        }
        if (hasText(preference.getDesiredExperienceLevel())) {
            spec = spec.and(JobSpecifications.experienceLevelEquals(preference.getDesiredExperienceLevel()));
        }
        if (preference.getMinSalary() != null) {
            spec = spec.and(JobSpecifications.salaryMinGte(preference.getMinSalary()));
        }
        if (preference.getMaxSalary() != null) {
            spec = spec.and(JobSpecifications.salaryMaxLte(preference.getMaxSalary()));
        }
        if (Boolean.TRUE.equals(preference.getRemoteOnly())) {
            spec = spec.and(JobSpecifications.remoteFriendly());
        }

        List<String> skills = jobNeedPreferenceService.deserializeList(preference.getPreferredSkills());
        if (!skills.isEmpty()) {
            spec = spec.and(JobSpecifications.hasAnySkill(skills));
        }

        List<String> excludedKeywords = jobNeedPreferenceService.deserializeList(preference.getExcludedKeywords());
        if (!excludedKeywords.isEmpty()) {
            spec = spec.and(JobSpecifications.keywordNotLike(excludedKeywords));
        }

        return jobRepository.findAll(
                        spec,
                        PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "postedDate"))
                )
                .stream()
                .filter(job -> job.getId() != null)
                .filter(job -> !jobAlertHistoryRepository.existsByUserIdAndJobId(user.getId(), job.getId()))
                .limit(MAX_JOBS_PER_EMAIL)
                .toList();
    }

    private boolean isEligibleForEmailAlerts(User user) {
        if (user == null || !hasText(user.getEmail())) {
            return false;
        }
        return notificationPreferenceRepository.findById(user.getId())
                .map(NotificationPreference::getEmailEnabled)
                .filter(Objects::nonNull)
                .orElse(false);
    }

    private void saveAlertHistory(User user, List<Job> jobs) {
        List<JobAlertHistory> histories = jobs.stream()
                .filter(job -> job.getId() != null)
                .filter(job -> !jobAlertHistoryRepository.existsByUserIdAndJobId(user.getId(), job.getId()))
                .map(job -> {
                    JobAlertHistory history = new JobAlertHistory();
                    history.setUserId(user.getId());
                    history.setJobId(job.getId());
                    history.setSentAt(LocalDateTime.now());
                    return history;
                })
                .toList();
        jobAlertHistoryRepository.saveAll(histories);
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
