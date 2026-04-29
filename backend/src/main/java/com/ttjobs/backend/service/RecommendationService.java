package com.ttjobs.backend.service;

import com.ttjobs.backend.dto.AiPredictionDTO;
import com.ttjobs.backend.dto.AiPredictionRequest;
import com.ttjobs.backend.dto.JobDTO;
import com.ttjobs.backend.entity.JobNeedPreference;
import com.ttjobs.backend.entity.Job;
import com.ttjobs.backend.entity.User;
import com.ttjobs.backend.repository.JobRepository;
import com.ttjobs.backend.repository.JobSpecifications;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
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
    private ObjectMapper objectMapper;
    @Autowired
    private RestTemplate restTemplate;

    @Value("${ttjobs.ai.base-url}")
    private String aiBaseUrl;

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

    public List<JobDTO> recommendByJobNeeds() {
        User currentUser = authContextService.requireCurrentUser();

        JobNeedPreference preference = jobNeedPreferenceService.getOrCreate(currentUser.getId());
        if (!jobNeedPreferenceService.hasConfiguredCriteria(preference)) {
            return List.of();
        }

        Specification<Job> spec = Specification.where(JobSpecifications.activeJobs())
                .and(JobSpecifications.statusEquals("open"));

        if (preference.getDesiredTitle() != null && !preference.getDesiredTitle().isBlank()) {
            spec = spec.and(JobSpecifications.keywordLike(preference.getDesiredTitle()));
        }
        if (preference.getDesiredLocation() != null && !preference.getDesiredLocation().isBlank()) {
            spec = spec.and(JobSpecifications.locationLike(preference.getDesiredLocation()));
        }
        if (preference.getDesiredCategory() != null && !preference.getDesiredCategory().isBlank()) {
            spec = spec.and(JobSpecifications.categoryIn(List.of(preference.getDesiredCategory())));
        }
        if (preference.getDesiredJobType() != null && !preference.getDesiredJobType().isBlank()) {
            spec = spec.and(JobSpecifications.jobTypeEquals(preference.getDesiredJobType()));
        }
        if (preference.getDesiredExperienceLevel() != null && !preference.getDesiredExperienceLevel().isBlank()) {
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

        return jobRepository.findAll(spec, PageRequest.of(0, MAX_JOBS))
                .stream()
                .map(job -> toDto(job, preference))
                .collect(Collectors.toList());
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
            dto.setMatchReasons(reasons);
            dto.setMatchScore(Math.min(98, 55 + reasons.size() * 9));
        }
        return dto;
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
