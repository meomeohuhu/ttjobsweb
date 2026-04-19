package com.ttjobs.backend.controller;

import com.ttjobs.backend.dto.RecruiterDashboardDTO;
import com.ttjobs.backend.service.JwtService;
import com.ttjobs.backend.service.RecruiterDashboardService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RecruiterDashboardController.class)
@AutoConfigureMockMvc(addFilters = false)
class RecruiterDashboardControllerApiTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RecruiterDashboardService recruiterDashboardService;

    @MockBean
    private JwtService jwtService;

    @Test
    void getDashboard_shouldReturnPayload() throws Exception {
        RecruiterDashboardDTO dto = new RecruiterDashboardDTO();
        dto.setOpenJobCount(3L);
        dto.setNewApplicationCount(12L);
        dto.setExpiringSoonJobCount(1L);
        dto.setApplicationStatusCounts(Map.of("submitted", 5L, "reviewing", 2L));

        when(recruiterDashboardService.getDashboard()).thenReturn(dto);

        mockMvc.perform(get("/api/recruiter/dashboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.openJobCount").value(3))
                .andExpect(jsonPath("$.newApplicationCount").value(12))
                .andExpect(jsonPath("$.applicationStatusCounts.submitted").value(5));
    }
}
