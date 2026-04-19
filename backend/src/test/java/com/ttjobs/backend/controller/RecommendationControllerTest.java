package com.ttjobs.backend.controller;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ttjobs.backend.dto.JobDTO;
import com.ttjobs.backend.service.JwtService;
import com.ttjobs.backend.service.RecommendationService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(RecommendationController.class)
@AutoConfigureMockMvc(addFilters = false)
class RecommendationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RecommendationService recommendationService;

    @MockBean
    private JwtService jwtService;

    @Test
    void recommendByCvText_shouldAcceptMessageFieldPayload() throws Exception {
        when(recommendationService.recommendByCvText(eq("java"))).thenReturn(List.of());

        mockMvc.perform(post("/api/recommendations/cv-text")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"message\":\"java\"}"))
                .andExpect(status().isOk());

        verify(recommendationService).recommendByCvText("java");
    }

    @Test
    void recommendByCvText_shouldAcceptRawTextPayload() throws Exception {
        when(recommendationService.recommendByCvText(eq("java"))).thenReturn(List.of(new JobDTO()));

        mockMvc.perform(post("/api/recommendations/cv-text")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("java"))
                .andExpect(status().isOk());

        verify(recommendationService).recommendByCvText("java");
    }
}
