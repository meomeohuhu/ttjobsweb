package com.ttjobs.backend.service;

import com.ttjobs.backend.dto.AiPredictionDTO;
import com.ttjobs.backend.dto.AiPredictionRequest;
import com.ttjobs.backend.dto.AiJobCandidateDTO;
import com.ttjobs.backend.dto.AiJobMatchDTO;
import com.ttjobs.backend.dto.AiJobMatchRequest;
import com.ttjobs.backend.dto.JobDTO;
import com.ttjobs.backend.entity.CandidateJobMatch;
import com.ttjobs.backend.entity.JobNeedPreference;
import com.ttjobs.backend.entity.Job;
import com.ttjobs.backend.entity.User;
import com.ttjobs.backend.repository.CandidateJobMatchRepository;
import com.ttjobs.backend.repository.JobRepository;
import com.ttjobs.backend.repository.JobSpecifications;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Map;
import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class RecommendationService {

    private static final Logger log = LoggerFactory.getLogger(RecommendationService.class);

    private static final int TOP_CATEGORIES = 3;
    private static final int MAX_JOBS = 30;
    private static final int MAX_AI_CANDIDATES = 120;
    private static final long JOB_NEED_CACHE_TTL_MILLIS = 120_000;
    private static final List<String> LABEL_MAPPING = List.of(
            "ACCOUNTANT",
            "ADVOCATE",
            "AGRICULTURE",
            "APPAREL",
            "ARTS",
            "AUTOMOBILE",
            "AVIATION",
            "BANKING",
            "BPO",
            "BUSINESS-DEVELOPMENT",
            "CHEF",
            "CONSTRUCTION",
            "CONSULTANT",
            "DESIGNER",
            "DIGITAL-MEDIA",
            "ENGINEERING",
            "FINANCE",
            "FITNESS",
            "HEALTHCARE",
            "HR",
            "INFORMATION-TECHNOLOGY",
            "PUBLIC-RELATIONS",
            "SALES",
            "TEACHER"
    );

    @Autowired
    private AuthContextService authContextService;
    @Autowired
    private JobNeedPreferenceService jobNeedPreferenceService;
    @Autowired
    private JobRepository jobRepository;
    @Autowired
    private CandidateJobMatchRepository candidateJobMatchRepository;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private RestTemplate restTemplate;

    @Value("${ttjobs.ai.base-url}")
    private String aiBaseUrl;

    private final Map<String, CachedRecommendation> jobNeedCache = new ConcurrentHashMap<>();

    public List<JobDTO> recommendByCv() {
        User currentUser = authContextService.requireCurrentUser();
        if (currentUser.getRole() != User.Role.CANDIDATE) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only candidate can request recommendations");
        }
        if (currentUser.getCvText() == null || currentUser.getCvText().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "CV text not found");
        }

        List<AiPredictionDTO> predictions = fetchPredictions(currentUser.getCvText());
        return findJobsFromPredictions(predictions);
    }

    public List<JobDTO> recommendByCvText(String cvText) {
        User currentUser = authContextService.requireCurrentUser();
        if (currentUser.getRole() != User.Role.CANDIDATE) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only candidate can request recommendations");
        }
        if (cvText == null || cvText.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "cvText is required");
        }

        List<AiPredictionDTO> predictions = fetchPredictions(cvText);
        return findJobsFromPredictions(predictions);
    }

    @Transactional
    public List<JobDTO> recommendByJobNeeds() {
        User currentUser = authContextService.requireCurrentUser();

        JobNeedPreference preference = jobNeedPreferenceService.getOrCreate(currentUser.getId());
        if (!jobNeedPreferenceService.hasConfiguredCriteria(preference)) {
            return List.of();
        }

        String cacheKey = buildJobNeedCacheKey(currentUser.getId(), preference);
        CachedRecommendation cached = jobNeedCache.get(cacheKey);
        if (cached != null && cached.expiresAtMillis > System.currentTimeMillis()) {
            List<JobDTO> activeCachedJobs = filterAcceptingApplications(cached.jobs());
            if (activeCachedJobs.size() == cached.jobs().size()) {
                return activeCachedJobs;
            }
            jobNeedCache.remove(cacheKey);
        }

        List<JobDTO> storedMatches = loadStoredJobNeedMatches(currentUser.getId(), preference);
        if (!storedMatches.isEmpty()) {
            return cacheJobNeedResult(cacheKey, storedMatches);
        }

        Specification<Job> spec = Specification.where(JobSpecifications.activeJobs())
                .and(JobSpecifications.statusEquals("open"));

        if (preference.getMinSalary() != null) {
            spec = spec.and(JobSpecifications.salaryMinGte(preference.getMinSalary()));
        }
        if (preference.getMaxSalary() != null) {
            spec = spec.and(JobSpecifications.salaryMaxLte(preference.getMaxSalary()));
        }

        List<String> excludedKeywords = Objects.requireNonNullElse(
                jobNeedPreferenceService.deserializeList(preference.getExcludedKeywords()), List.of());
        if (!excludedKeywords.isEmpty()) {
            spec = spec.and(JobSpecifications.keywordNotLike(excludedKeywords));
        }
        if (Boolean.TRUE.equals(preference.getRemoteOnly())) {
            spec = spec.and(JobSpecifications.remoteFriendly());
        } else if (Boolean.FALSE.equals(preference.getRemoteOnly())) {
            spec = spec.and(JobSpecifications.nonRemote());
        }

        List<Job> candidates = jobRepository.findAll(
                spec,
                PageRequest.of(0, MAX_AI_CANDIDATES, Sort.by(Sort.Direction.DESC, "postedDate"))
        ).getContent();
        if (candidates.isEmpty()) {
            return cacheJobNeedResult(cacheKey, List.of());
        }

        List<AiJobMatchDTO> aiMatches = fetchJobNeedMatches(preference, candidates);
        if (!aiMatches.isEmpty()) {
            Map<Long, Job> jobsById = candidates.stream()
                    .collect(Collectors.toMap(Job::getId, Function.identity()));
            List<JobDTO> result = aiMatches.stream()
                    .map(match -> {
                        Job job = jobsById.get(match.getJobId());
                        return job == null ? null : toDto(job, preference, match);
                    })
                    .filter(Objects::nonNull)
                    .limit(MAX_JOBS)
                    .collect(Collectors.toList());
            storeJobNeedMatches(currentUser.getId(), preference, aiMatches, jobsById);
            return cacheJobNeedResult(cacheKey, result);
        }

        List<JobDTO> result = candidates.stream()
                .map(job -> toDto(job, preference))
                .sorted((first, second) -> Integer.compare(
                        Objects.requireNonNullElse(second.getMatchScore(), 0),
                        Objects.requireNonNullElse(first.getMatchScore(), 0)))
                .limit(MAX_JOBS)
                .collect(Collectors.toList());
        return cacheJobNeedResult(cacheKey, result);
    }

    private List<JobDTO> findJobsFromPredictions(List<AiPredictionDTO> predictions) {
        List<String> categories = predictions.stream()
                .map(AiPredictionDTO::getCategory)
                .map(this::mapLabelToCategory)
                .filter(c -> c != null && !c.isBlank())
                .limit(TOP_CATEGORIES)
                .toList();

        if (categories.isEmpty()) {
            return List.of();
        }

        Specification<Job> spec = Specification.where(JobSpecifications.activeJobs())
                .and(JobSpecifications.statusEquals("open"))
                .and(JobSpecifications.categoryIn(categories));

        return jobRepository.findAll(spec, PageRequest.of(0, MAX_JOBS))
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    private List<AiPredictionDTO> fetchPredictions(String cvText) {
        try {
            AiPredictionRequest request = new AiPredictionRequest();
            request.setCvText(cvText);

            RequestEntity<AiPredictionRequest> entity = RequestEntity
                    .post(aiBaseUrl + "/ai/predict")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request);

            String response = restTemplate.exchange(entity, String.class).getBody();
            if (response == null || response.isBlank()) {
                return List.of();
            }

            List<List<Object>> raw = objectMapper.readValue(response, new TypeReference<>() {});
            List<AiPredictionDTO> predictions = new ArrayList<>();
            for (List<Object> item : raw) {
                if (item.size() < 2) {
                    continue;
                }
                String category = item.get(0) == null ? null : item.get(0).toString();
                Double score = null;
                if (item.get(1) instanceof Number number) {
                    score = number.doubleValue();
                }
                AiPredictionDTO dto = new AiPredictionDTO();
                dto.setCategory(category);
                dto.setScore(score);
                predictions.add(dto);
            }
            return predictions;
        } catch (Exception ex) {
            log.warn("AI service unavailable, returning empty recommendations. cause={}", ex.toString());
            return List.of();
        }
    }

    private String mapLabelToCategory(String labelOrCategory) {
        if (labelOrCategory == null || labelOrCategory.isBlank()) {
            return null;
        }
        String value = labelOrCategory.trim();
        if (value.startsWith("LABEL_")) {
            try {
                int index = Integer.parseInt(value.substring("LABEL_".length()));
                if (index >= 0 && index < LABEL_MAPPING.size()) {
                    return LABEL_MAPPING.get(index);
                }
                return null;
            } catch (NumberFormatException ex) {
                return null;
            }
        }
        return value;
    }

    private JobDTO toDto(Job job) {
        return toDto(job, null);
    }

    private JobDTO toDto(Job job, JobNeedPreference preference) {
        return toDto(job, preference, null);
    }

    private JobDTO toDto(Job job, JobNeedPreference preference, AiJobMatchDTO aiMatch) {
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
        }
        if (preference != null) {
            List<String> reasons = resolveMatchReasons(job, preference);
            if (aiMatch != null && aiMatch.getReasons() != null && !aiMatch.getReasons().isEmpty()) {
                reasons = new ArrayList<>(reasons);
                reasons.add(0, "AI phan tich ngu canh phu hop");
            }
            dto.setMatchReasons(reasons);
            dto.setMatchReason(String.join("; ", reasons));
            dto.setMatchScore(calculateFinalMatchScore(job, preference, aiMatch, reasons));
        }
        return dto;
    }

    private int calculateFinalMatchScore(Job job, JobNeedPreference preference, AiJobMatchDTO aiMatch, List<String> reasons) {
        int fallbackScore = Math.min(98, 55 + reasons.size() * 9);
        int aiScore = aiMatch != null && aiMatch.getScore() != null
                ? Math.min(98, Math.max(1, aiMatch.getScore()))
                : fallbackScore;
        int skillScore = calculateSkillScore(job, preference);
        if (skillScore <= 0) {
            return aiScore;
        }
        return Math.min(98, Math.max(1, Math.round(aiScore * 0.6f + skillScore * 0.4f)));
    }

    private int calculateSkillScore(Job job, JobNeedPreference preference) {
        if (job.getSkills() == null || job.getSkills().isEmpty() || preference == null) {
            return 0;
        }
        List<String> preferredSkills = Objects.requireNonNullElse(
                jobNeedPreferenceService.deserializeList(preference.getPreferredSkills()), List.of());
        if (preferredSkills.isEmpty()) {
            return 0;
        }
        long matched = job.getSkills().stream()
                .filter(skill -> preferredSkills.stream().anyMatch(preferred -> sameIgnoreCase(skill.getName(), preferred)))
                .count();
        return Math.round((matched * 100f) / job.getSkills().size());
    }

    private List<AiJobMatchDTO> fetchJobNeedMatches(JobNeedPreference preference, List<Job> jobs) {
        try {
            AiJobMatchRequest request = new AiJobMatchRequest();
            request.setNeedText(buildNeedText(preference));
            request.setLimit(MAX_JOBS);
            request.setJobs(jobs.stream()
                    .map(job -> new AiJobCandidateDTO(job.getId(), buildJobText(job)))
                    .collect(Collectors.toList()));

            RequestEntity<AiJobMatchRequest> entity = RequestEntity
                    .post(aiBaseUrl + "/ai/match-jobs")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request);

            AiJobMatchDTO[] response = restTemplate.exchange(entity, AiJobMatchDTO[].class).getBody();
            return response == null ? List.of() : List.of(response);
        } catch (Exception ex) {
            log.warn("AI job matching unavailable, using fallback recommendation scoring. cause={}", ex.toString());
            return List.of();
        }
    }

    private List<JobDTO> loadStoredJobNeedMatches(Long userId, JobNeedPreference preference) {
        if (preference.getUpdatedAt() == null) {
            return List.of();
        }
        return candidateJobMatchRepository
                .findByUserIdAndPreferenceUpdatedAtOrderByScoreDescCreatedAtDesc(
                        userId,
                        preference.getUpdatedAt(),
                        PageRequest.of(0, MAX_JOBS)
                )
                .stream()
                .filter(match -> match.getJob() != null)
                .filter(match -> isAcceptingApplications(match.getJob()))
                .map(match -> toDto(match.getJob(), preference, toAiMatch(match)))
                .collect(Collectors.toList());
    }

    private void storeJobNeedMatches(
            Long userId,
            JobNeedPreference preference,
            List<AiJobMatchDTO> matches,
            Map<Long, Job> jobsById
    ) {
        if (preference.getUpdatedAt() == null || matches.isEmpty()) {
            return;
        }
        candidateJobMatchRepository.deleteByUserId(userId);
        List<CandidateJobMatch> entities = matches.stream()
                .limit(MAX_JOBS)
                .map(match -> {
                    Job job = jobsById.get(match.getJobId());
                    if (job == null || !isAcceptingApplications(job)) {
                        return null;
                    }
                    CandidateJobMatch entity = new CandidateJobMatch();
                    entity.setUserId(userId);
                    entity.setJob(job);
                    entity.setPreferenceUpdatedAt(preference.getUpdatedAt());
                    entity.setScore(Objects.requireNonNullElse(match.getScore(), 0));
                    entity.setReasons(serializeReasons(match.getReasons()));
                    entity.setCreatedAt(java.time.LocalDateTime.now());
                    return entity;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        candidateJobMatchRepository.saveAll(entities);
    }

    private AiJobMatchDTO toAiMatch(CandidateJobMatch match) {
        AiJobMatchDTO dto = new AiJobMatchDTO();
        dto.setJobId(match.getJob().getId());
        dto.setScore(match.getScore());
        dto.setReasons(deserializeReasons(match.getReasons()));
        return dto;
    }

    private String serializeReasons(List<String> reasons) {
        if (reasons == null || reasons.isEmpty()) {
            return "";
        }
        return reasons.stream()
                .filter(reason -> reason != null && !reason.isBlank())
                .collect(Collectors.joining("\n"));
    }

    private List<String> deserializeReasons(String reasons) {
        if (reasons == null || reasons.isBlank()) {
            return List.of();
        }
        return Arrays.stream(reasons.split("\\R"))
                .map(String::trim)
                .filter(reason -> !reason.isBlank())
                .toList();
    }

    private List<JobDTO> cacheJobNeedResult(String cacheKey, List<JobDTO> jobs) {
        List<JobDTO> snapshot = List.copyOf(filterAcceptingApplications(jobs));
        jobNeedCache.put(cacheKey, new CachedRecommendation(
                snapshot,
                System.currentTimeMillis() + JOB_NEED_CACHE_TTL_MILLIS
        ));
        return snapshot;
    }

    private List<JobDTO> filterAcceptingApplications(List<JobDTO> jobs) {
        if (jobs == null || jobs.isEmpty()) {
            return List.of();
        }
        LocalDateTime now = LocalDateTime.now();
        return jobs.stream()
                .filter(job -> job.getApplicationDeadline() == null || !job.getApplicationDeadline().isBefore(now))
                .toList();
    }

    private boolean isAcceptingApplications(Job job) {
        if (job == null || job.getDeletedAt() != null) {
            return false;
        }
        return job.getApplicationDeadline() == null || !job.getApplicationDeadline().isBefore(LocalDateTime.now());
    }

    private String buildJobNeedCacheKey(Long userId, JobNeedPreference preference) {
        return String.join("|", List.of(
                Objects.toString(userId, ""),
                Objects.toString(preference.getUpdatedAt(), ""),
                Objects.toString(preference.getDesiredTitle(), ""),
                Objects.toString(preference.getDesiredLocation(), ""),
                Objects.toString(preference.getDesiredCategory(), ""),
                Objects.toString(preference.getDesiredJobType(), ""),
                Objects.toString(preference.getDesiredExperienceLevel(), ""),
                Objects.toString(preference.getMinSalary(), ""),
                Objects.toString(preference.getMaxSalary(), ""),
                Objects.toString(preference.getPreferredSkills(), ""),
                Objects.toString(preference.getExcludedKeywords(), ""),
                Objects.toString(preference.getRemoteOnly(), "")
        ));
    }

    private record CachedRecommendation(List<JobDTO> jobs, long expiresAtMillis) {
    }

    private String buildNeedText(JobNeedPreference preference) {
        List<String> preferredSkills = Objects.requireNonNullElse(
                jobNeedPreferenceService.deserializeList(preference.getPreferredSkills()), List.of());
        String skillContext = preferredSkills.stream()
                .map(this::expandSkillContext)
                .filter(value -> !value.isBlank())
                .distinct()
                .collect(Collectors.joining(", "));
        return String.join(". ", List.of(
                "Desired title: " + Objects.toString(preference.getDesiredTitle(), ""),
                "Desired category: " + Objects.toString(preference.getDesiredCategory(), ""),
                "Desired job type: " + Objects.toString(preference.getDesiredJobType(), ""),
                "Desired experience: " + Objects.toString(preference.getDesiredExperienceLevel(), ""),
                "Preferred skills: " + String.join(", ", preferredSkills),
                "Related skill context: " + skillContext,
                "Location: " + Objects.toString(preference.getDesiredLocation(), "")
        ));
    }

    private String expandSkillContext(String skill) {
        if (skill == null || skill.isBlank()) {
            return "";
        }
        String normalized = skill.trim().toLowerCase(Locale.ROOT);
        return switch (normalized) {
            case "java" -> "Java, Spring Boot, backend developer, software engineer, information technology, IT";
            case "spring", "spring boot" -> "Spring Boot, Java, backend developer, REST API, information technology";
            case "react", "reactjs", "react.js" -> "React, JavaScript, frontend developer, web developer, information technology";
            case "node", "nodejs", "node.js" -> "Node.js, JavaScript, backend developer, API, information technology";
            case "python" -> "Python, backend developer, data, automation, information technology";
            case "sql" -> "SQL, database, backend developer, data analyst, information technology";
            default -> skill.trim();
        };
    }

    private String buildJobText(Job job) {
        String companyName = job.getCompany() == null ? "" : Objects.toString(job.getCompany().getName(), "");
        String companyIndustry = job.getCompany() == null ? "" : Objects.toString(job.getCompany().getIndustry(), "");
        String skills = job.getSkills() == null ? "" : job.getSkills().stream()
                .map(skill -> Objects.toString(skill.getName(), ""))
                .filter(value -> !value.isBlank())
                .collect(Collectors.joining(", "));
        return String.join(". ", List.of(
                "Title: " + Objects.toString(job.getTitle(), ""),
                "Description: " + Objects.toString(job.getDescription(), ""),
                "Category: " + Objects.toString(job.getCategory(), ""),
                "Job type: " + Objects.toString(job.getJobType(), ""),
                "Experience: " + Objects.toString(job.getExperienceLevel(), ""),
                "Location: " + Objects.toString(job.getLocation(), ""),
                "Company: " + companyName,
                "Company industry: " + companyIndustry,
                "Skills: " + skills
        ));
    }

    private List<String> resolveMatchReasons(Job job, JobNeedPreference preference) {
        List<String> reasons = new ArrayList<>();
        if (containsIgnoreCase(job.getTitle(), preference.getDesiredTitle())
                || containsIgnoreCase(job.getDescription(), preference.getDesiredTitle())) {
            reasons.add("Trùng vị trí mong muốn");
        }
        if (containsIgnoreCase(job.getLocation(), preference.getDesiredLocation())) {
            reasons.add("Đúng khu vực ưu tiên");
        }
        if (sameIgnoreCase(job.getCategory(), preference.getDesiredCategory())) {
            reasons.add("Trùng ngành nghề");
        }
        if (sameIgnoreCase(job.getJobType(), preference.getDesiredJobType())) {
            reasons.add("Đúng loại hình làm việc");
        }
        if (sameIgnoreCase(job.getExperienceLevel(), preference.getDesiredExperienceLevel())) {
            reasons.add("Phù hợp mức kinh nghiệm");
        }
        if (preference.getMinSalary() != null && job.getSalaryMax() != null
                && job.getSalaryMax().compareTo(preference.getMinSalary()) >= 0) {
            reasons.add("Đạt mức lương tối thiểu");
        }
        List<String> preferredSkills = Objects.requireNonNullElse(
                jobNeedPreferenceService.deserializeList(preference.getPreferredSkills()), List.of());
        if (!preferredSkills.isEmpty() && job.getSkills() != null
                && job.getSkills().stream().anyMatch(skill -> preferredSkills.stream()
                        .anyMatch(preferred -> sameIgnoreCase(skill.getName(), preferred)))) {
            reasons.add("Trung ky nang uu tien");
        }
        if (Boolean.TRUE.equals(preference.getRemoteOnly())
                && (containsIgnoreCase(job.getLocation(), "remote")
                || containsIgnoreCase(job.getDescription(), "remote")
                || containsIgnoreCase(job.getDescription(), "hybrid"))) {
            reasons.add("Có yếu tố remote/hybrid");
        }
        if (reasons.isEmpty()) {
            reasons.add("Phù hợp với nhu cầu đã lưu");
        }
        return reasons;
    }

    private boolean containsIgnoreCase(String value, String query) {
        return value != null && query != null && !query.isBlank()
                && value.toLowerCase(Locale.ROOT).contains(query.toLowerCase(Locale.ROOT));
    }

    private boolean sameIgnoreCase(String first, String second) {
        if (first == null || second == null || second.isBlank()) {
            return false;
        }
        return Objects.equals(first.trim().toLowerCase(Locale.ROOT), second.trim().toLowerCase(Locale.ROOT));
    }
}
