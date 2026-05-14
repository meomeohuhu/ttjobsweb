package com.ttjobs.backend.service;

import com.ttjobs.backend.dto.ai.AiNormalizeRequest;
import com.ttjobs.backend.dto.ai.AiSignalDTO;
import com.ttjobs.backend.entity.CandidateProfileSignal;
import com.ttjobs.backend.entity.Job;
import com.ttjobs.backend.entity.JobRequirementSignal;
import com.ttjobs.backend.entity.User;
import com.ttjobs.backend.repository.CandidateProfileSignalRepository;
import com.ttjobs.backend.repository.JobRepository;
import com.ttjobs.backend.repository.JobRequirementSignalRepository;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AiNormalizeService {

    @Autowired
    private AuthContextService authContextService;
    @Autowired
    private CandidateProfileSignalRepository candidateProfileSignalRepository;
    @Autowired
    private JobRequirementSignalRepository jobRequirementSignalRepository;
    @Autowired
    private JobRepository jobRepository;

    @Value("${ttjobs.ai.llm.enabled:false}")
    private boolean llmEnabled;

    @Transactional
    public AiSignalDTO normalizeCv(AiNormalizeRequest request) {
        User currentUser = authContextService.requireCurrentUser();
        String text = requireText(request == null ? null : request.getText(), currentUser.getCvText());
        CandidateProfileSignal signal = new CandidateProfileSignal();
        signal.setUser(currentUser);
        applyRuleBasedSignal(signal, text);
        return toDto(candidateProfileSignalRepository.save(signal), currentUser.getId());
    }

    @Transactional
    public AiSignalDTO normalizeJob(AiNormalizeRequest request) {
        if (request == null || request.getJobId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "jobId is required");
        }
        Job job = jobRepository.findByIdAndDeletedAtIsNull(request.getJobId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Job not found"));
        String text = requireText(request.getText(), buildJobText(job));
        JobRequirementSignal signal = jobRequirementSignalRepository.findByJobId(job.getId()).orElseGet(() -> {
            JobRequirementSignal created = new JobRequirementSignal();
            created.setJob(job);
            return created;
        });
        applyRuleBasedSignal(signal, text, job);
        return toDto(jobRequirementSignalRepository.save(signal), job.getId());
    }

    private String requireText(String preferred, String fallback) {
        String value = hasText(preferred) ? preferred : fallback;
        if (!hasText(value)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "text is required");
        }
        return value.trim();
    }

    private void applyRuleBasedSignal(CandidateProfileSignal signal, String text) {
        SignalParts parts = extract(text);
        signal.setRawText(text);
        signal.setSource(llmEnabled ? "LLM_FALLBACK_RULE_BASED" : "RULE_BASED");
        signal.setNormalizedTitle(parts.title());
        signal.setSeniority(parts.seniority());
        signal.setSkills(join(parts.skills()));
        signal.setIndustries(join(parts.industries()));
        signal.setLocations(join(parts.locations()));
        signal.setLanguages(join(parts.languages()));
        signal.setEvidence(firstEvidence(text));
        signal.setCurrency("VND");
    }

    private void applyRuleBasedSignal(JobRequirementSignal signal, String text, Job job) {
        SignalParts parts = extract(text);
        signal.setRawText(text);
        signal.setSource(llmEnabled ? "LLM_FALLBACK_RULE_BASED" : "RULE_BASED");
        signal.setNormalizedTitle(hasText(job.getTitle()) ? normalizeTitle(job.getTitle()) : parts.title());
        signal.setSeniority(hasText(job.getExperienceLevel()) ? job.getExperienceLevel() : parts.seniority());
        signal.setSkills(join(parts.skills()));
        signal.setIndustries(join(List.of(Objects.toString(job.getCategory(), ""))));
        signal.setLocations(join(hasText(job.getLocation()) ? List.of(job.getLocation()) : parts.locations()));
        signal.setSalaryMin(job.getSalaryMin());
        signal.setSalaryMax(job.getSalaryMax());
        signal.setCurrency(Objects.toString(job.getCurrency(), "VND"));
        signal.setLanguages(join(parts.languages()));
        signal.setEvidence(firstEvidence(text));
    }

    private SignalParts extract(String text) {
        String lower = text.toLowerCase(Locale.ROOT);
        List<String> skills = List.of("Java", "Spring Boot", "React", "Node.js", "Python", "SQL", "Docker", "AWS", "JavaScript", "TypeScript")
                .stream().filter(skill -> lower.contains(skill.toLowerCase(Locale.ROOT))).distinct().toList();
        List<String> locations = List.of("Ho Chi Minh City", "Ha Noi", "Da Nang", "Remote")
                .stream().filter(location -> lower.contains(location.toLowerCase(Locale.ROOT))
                        || lower.contains(location.replace("Ho Chi Minh City", "hồ chí minh").toLowerCase(Locale.ROOT))
                        || lower.contains(location.replace("Ha Noi", "hà nội").toLowerCase(Locale.ROOT)))
                .toList();
        String seniority = lower.contains("senior") ? "senior" : lower.contains("junior") ? "junior" : "middle";
        String title = lower.contains("backend") ? "backend developer"
                : lower.contains("frontend") ? "frontend developer"
                : lower.contains("data") ? "data specialist"
                : "software professional";
        return new SignalParts(title, seniority, skills, List.of("information technology"), locations, List.of("Vietnamese"));
    }

    private String normalizeTitle(String title) {
        String lower = title.toLowerCase(Locale.ROOT);
        if (lower.contains("backend")) return "backend developer";
        if (lower.contains("frontend")) return "frontend developer";
        return title.trim();
    }

    private String buildJobText(Job job) {
        return String.join(". ", List.of(
                Objects.toString(job.getTitle(), ""),
                Objects.toString(job.getDescription(), ""),
                Objects.toString(job.getCategory(), ""),
                Objects.toString(job.getLocation(), "")
        ));
    }

    private AiSignalDTO toDto(CandidateProfileSignal signal, Long ownerId) {
        AiSignalDTO dto = baseDto(signal.getId(), ownerId, signal.getNormalizedTitle(), signal.getSeniority(),
                signal.getSkills(), signal.getIndustries(), signal.getLocations(), signal.getSalaryMin(),
                signal.getSalaryMax(), signal.getCurrency(), signal.getLanguages(), signal.getEvidence(),
                signal.getSource(), signal.getUpdatedAt());
        return dto;
    }

    private AiSignalDTO toDto(JobRequirementSignal signal, Long ownerId) {
        return baseDto(signal.getId(), ownerId, signal.getNormalizedTitle(), signal.getSeniority(),
                signal.getSkills(), signal.getIndustries(), signal.getLocations(), signal.getSalaryMin(),
                signal.getSalaryMax(), signal.getCurrency(), signal.getLanguages(), signal.getEvidence(),
                signal.getSource(), signal.getUpdatedAt());
    }

    private AiSignalDTO baseDto(Long id, Long ownerId, String title, String seniority, String skills, String industries,
                                String locations, BigDecimal salaryMin, BigDecimal salaryMax, String currency,
                                String languages, String evidence, String source, java.time.LocalDateTime updatedAt) {
        AiSignalDTO dto = new AiSignalDTO();
        dto.setId(id);
        dto.setOwnerId(ownerId);
        dto.setNormalizedTitle(title);
        dto.setSeniority(seniority);
        dto.setSkills(split(skills));
        dto.setIndustries(split(industries));
        dto.setLocations(split(locations));
        dto.setSalaryMin(salaryMin);
        dto.setSalaryMax(salaryMax);
        dto.setCurrency(currency);
        dto.setLanguages(split(languages));
        dto.setEvidence(split(evidence));
        dto.setSource(source);
        dto.setUpdatedAt(updatedAt);
        return dto;
    }

    private String firstEvidence(String text) {
        return Arrays.stream(text.split("\\R")).map(String::trim).filter(this::hasText).limit(3).collect(Collectors.joining("\n"));
    }
    private String join(List<String> values) { return values == null ? "" : values.stream().filter(this::hasText).distinct().collect(Collectors.joining("\n")); }
    private List<String> split(String raw) { return raw == null || raw.isBlank() ? List.of() : Arrays.stream(raw.split("\\R")).map(String::trim).filter(this::hasText).toList(); }
    private boolean hasText(String value) { return value != null && !value.isBlank(); }
    private record SignalParts(String title, String seniority, List<String> skills, List<String> industries, List<String> locations, List<String> languages) {}
}
