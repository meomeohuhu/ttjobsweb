package com.ttjobs.backend.controller;

import com.ttjobs.backend.dto.recruiter.RecruiterDashboardDTO;
import com.ttjobs.backend.dto.recruiter.RecruiterApplicationDetailDTO;
import com.ttjobs.backend.dto.recruiter.RecruiterActivityLogDTO;
import com.ttjobs.backend.dto.recruiter.RecruiterCompanyDTO;
import com.ttjobs.backend.dto.recruiter.RecruiterJobDTO;
import com.ttjobs.backend.service.JwtService;
import com.ttjobs.backend.service.RecruiterDashboardService;
import com.ttjobs.backend.service.RecruiterWorkspaceService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;
import java.util.List;

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
    private RecruiterWorkspaceService recruiterWorkspaceService;

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

    @Test
    void getManagedCompanies_shouldReturnPayload() throws Exception {
        RecruiterCompanyDTO dto = new RecruiterCompanyDTO();
        dto.setId(10L);
        dto.setName("Acme");
        dto.setVerificationStatus("VERIFIED");
        dto.setOpenJobCount(2L);

        when(recruiterWorkspaceService.getManagedCompanies()).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/recruiter/companies"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(10))
                .andExpect(jsonPath("$[0].name").value("Acme"))
                .andExpect(jsonPath("$[0].verificationStatus").value("VERIFIED"))
                .andExpect(jsonPath("$[0].openJobCount").value(2));
    }

    @Test
    void getManagedJobs_shouldPassFilters() throws Exception {
        RecruiterJobDTO dto = new RecruiterJobDTO();
        dto.setId(20L);
        dto.setTitle("Backend");
        dto.setApplicationCount(4L);

        when(recruiterWorkspaceService.getManagedJobs(10L, "open", "java", 0, 50))
                .thenReturn(List.of(dto));

        mockMvc.perform(get("/api/recruiter/jobs")
                        .param("companyId", "10")
                        .param("status", "open")
                        .param("keyword", "java"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(20))
                .andExpect(jsonPath("$[0].applicationCount").value(4));
    }

    @Test
    void getManagedApplicationDetail_shouldReturnPayload() throws Exception {
        RecruiterApplicationDetailDTO dto = new RecruiterApplicationDetailDTO();
        dto.setId(30L);
        dto.setCandidateName("Candidate");
        dto.setHasCv(true);

        when(recruiterWorkspaceService.getManagedApplicationDetail(30L)).thenReturn(dto);

        mockMvc.perform(get("/api/recruiter/applications/30"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(30))
                .andExpect(jsonPath("$.candidateName").value("Candidate"))
                .andExpect(jsonPath("$.hasCv").value(true));
    }

    @Test
    void getRecentActivities_shouldReturnPayload() throws Exception {
        RecruiterActivityLogDTO dto = new RecruiterActivityLogDTO();
        dto.setId(40L);
        dto.setActionType("JOB_CREATED");
        dto.setTitle("Tạo job mới");

        when(recruiterDashboardService.getRecentActivities(5)).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/recruiter/activity").param("limit", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(40))
                .andExpect(jsonPath("$[0].actionType").value("JOB_CREATED"))
                .andExpect(jsonPath("$[0].title").value("Tạo job mới"));
    }
}

