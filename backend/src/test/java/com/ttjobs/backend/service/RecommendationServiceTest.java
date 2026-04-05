package com.ttjobs.backend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ttjobs.backend.dto.JobDTO;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RecommendationServiceTest {

    @Mock
    private AuthContextService authContextService;
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
                .thenReturn(List.of(List.of("INFORMATION-TECHNOLOGY", 0.9)));
        when(jobRepository.findAll(any(org.springframework.data.jpa.domain.Specification.class), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(job)));

        ReflectionTestUtils.setField(recommendationService, "aiBaseUrl", "http://ai");

        List<JobDTO> result = recommendationService.recommendByCv();
        assertEquals(1, result.size());
        assertEquals("IT Job", result.get(0).getTitle());
    }
}
