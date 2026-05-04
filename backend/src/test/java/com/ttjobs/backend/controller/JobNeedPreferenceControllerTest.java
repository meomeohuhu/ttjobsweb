package com.ttjobs.backend.controller;

import com.ttjobs.backend.dto.JobNeedPreferenceDTO;
import com.ttjobs.backend.dto.JobNeedPreferenceRequest;
import com.ttjobs.backend.service.JobNeedPreferenceService;
import com.ttjobs.backend.service.JwtService;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(JobNeedPreferenceController.class)
@AutoConfigureMockMvc(addFilters = false)
class JobNeedPreferenceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JobNeedPreferenceService jobNeedPreferenceService;

    @MockBean
    private JwtService jwtService;

    @Test
    void getMyPreferences_shouldReturnOk() throws Exception {
        when(jobNeedPreferenceService.getMyPreferences()).thenReturn(new JobNeedPreferenceDTO());

        mockMvc.perform(get("/api/job-needs/preferences"))
                .andExpect(status().isOk());

        verify(jobNeedPreferenceService).getMyPreferences();
    }

    @Test
    void updateMyPreferences_shouldReturnOk() throws Exception {
        when(jobNeedPreferenceService.updateMyPreferences(any(JobNeedPreferenceRequest.class)))
                .thenReturn(new JobNeedPreferenceDTO());

        mockMvc.perform(put("/api/job-needs/preferences")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "desiredTitle": "Backend Engineer",
                                  "minSalary": 15000000,
                                  "maxSalary": 30000000,
                                  "preferredSkills": ["Java", "Spring Boot"],
                                  "excludedKeywords": ["Intern"],
                                  "remoteOnly": true
                                }
                                """))
                .andExpect(status().isOk());

        verify(jobNeedPreferenceService).updateMyPreferences(any(JobNeedPreferenceRequest.class));
    }
}
