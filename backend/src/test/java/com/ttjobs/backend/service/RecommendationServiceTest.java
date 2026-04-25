package com.ttjobs.backend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ttjobs.backend.dto.JobDTO;
import com.ttjobs.backend.entity.JobNeedPreference;
import com.ttjobs.backend.entity.Job;
import com.ttjobs.backend.entity.User;
import com.ttjobs.backend.repository.JobRepository;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.RestClientException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RecommendationServiceTest {

    @Mock
    private AuthContextService authContextService;
    @Mock
    private JobNeedPreferenceService jobNeedPreferenceService;
    @Mock
    private JobRepository jobRepository;
    @Mock
    private ObjectMapper objectMapper;
    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private RecommendationService recommendationService;

    @Test
    void recommendByCv_shouldReturnJobs() throws Exception {
        User candidate = new User();
        candidate.setId(1L);
        candidate.setRole(User.Role.CANDIDATE);
        candidate.setCvText("text");

        Job job = new Job();
        job.setId(10L);
        job.setTitle("IT Job");
        job.setCategory("INFORMATION-TECHNOLOGY");

        when(authContextService.requireCurrentUser()).thenReturn(candidate);
        when(restTemplate.exchange(any(org.springframework.http.RequestEntity.class), any(Class.class)))
                .thenReturn(ResponseEntity.ok("[[\"INFORMATION-TECHNOLOGY\",0.9]]"));
        when(objectMapper.readValue(any(String.class), any(com.fasterxml.jackson.core.type.TypeReference.class)))
                .thenReturn(List.of(List.of("LABEL_20", 0.9)));
        when(jobRepository.findAll(any(org.springframework.data.jpa.domain.Specification.class), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(job)));

        ReflectionTestUtils.setField(recommendationService, "aiBaseUrl", "http://ai");

        List<JobDTO> result = recommendationService.recommendByCv();
        assertEquals(1, result.size());
        assertEquals("IT Job", result.get(0).getTitle());
    }

    @Test
    void recommendByCvText_shouldReturnJobs() throws Exception {
        User candidate = new User();
        candidate.setId(3L);
        candidate.setRole(User.Role.CANDIDATE);

        Job job = new Job();
        job.setId(20L);
        job.setTitle("Finance Job");
        job.setCategory("FINANCE");

        when(authContextService.requireCurrentUser()).thenReturn(candidate);
        when(restTemplate.exchange(any(org.springframework.http.RequestEntity.class), any(Class.class)))
                .thenReturn(ResponseEntity.ok("[[\"LABEL_16\",0.8]]"));
        when(objectMapper.readValue(any(String.class), any(com.fasterxml.jackson.core.type.TypeReference.class)))
                .thenReturn(List.of(List.of("LABEL_16", 0.8)));
        when(jobRepository.findAll(any(org.springframework.data.jpa.domain.Specification.class), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(job)));

        ReflectionTestUtils.setField(recommendationService, "aiBaseUrl", "http://ai");

        List<JobDTO> result = recommendationService.recommendByCvText("cv text");
        assertEquals(1, result.size());
        assertEquals("Finance Job", result.get(0).getTitle());
    }

    @Test
    void recommendByCv_shouldIgnoreInvalidLabel() throws Exception {
        User candidate = new User();
        candidate.setId(2L);
        candidate.setRole(User.Role.CANDIDATE);
        candidate.setCvText("text");

        when(authContextService.requireCurrentUser()).thenReturn(candidate);
        when(restTemplate.exchange(any(org.springframework.http.RequestEntity.class), any(Class.class)))
                .thenReturn(ResponseEntity.ok("[[\"LABEL_99\",0.9]]"));
        when(objectMapper.readValue(any(String.class), any(com.fasterxml.jackson.core.type.TypeReference.class)))
                .thenReturn(List.of(List.of("LABEL_99", 0.9)));

        ReflectionTestUtils.setField(recommendationService, "aiBaseUrl", "http://ai");

        List<JobDTO> result = recommendationService.recommendByCv();
        assertEquals(0, result.size());
    }

    @Test
    void recommendByCvText_shouldFallbackToEmptyListWhenAiServiceFails() {
        User candidate = new User();
        candidate.setId(4L);
        candidate.setRole(User.Role.CANDIDATE);

        when(authContextService.requireCurrentUser()).thenReturn(candidate);
        when(restTemplate.exchange(any(org.springframework.http.RequestEntity.class), any(Class.class)))
                .thenThrow(new RestClientException("down"));

        ReflectionTestUtils.setField(recommendationService, "aiBaseUrl", "http://ai");

        List<JobDTO> result = recommendationService.recommendByCvText("cv text");

        assertEquals(0, result.size());
    }

    @Test
    void recommendByJobNeeds_shouldReturnJobs() {
        User candidate = new User();
        candidate.setId(5L);
        candidate.setRole(User.Role.CANDIDATE);

        JobNeedPreference preference = new JobNeedPreference();
        preference.setUserId(5L);
        preference.setDesiredTitle("Backend");
        preference.setDesiredLocation("Hà Nội");
        preference.setRemoteOnly(true);

        Job job = new Job();
        job.setId(30L);
        job.setTitle("Backend Engineer");
        job.setLocation("Hà Nội");
        job.setStatus("open");
        job.setCategory("INFORMATION-TECHNOLOGY");
        job.setDescription("Remote backend role");

        when(authContextService.requireCurrentUser()).thenReturn(candidate);
        when(jobNeedPreferenceService.getOrCreate(5L)).thenReturn(preference);
        when(jobNeedPreferenceService.hasConfiguredCriteria(preference)).thenReturn(true);
        when(jobRepository.findAll(any(org.springframework.data.jpa.domain.Specification.class), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(job)));

        List<JobDTO> result = recommendationService.recommendByJobNeeds();

        assertEquals(1, result.size());
        assertEquals("Backend Engineer", result.get(0).getTitle());
    }

}
