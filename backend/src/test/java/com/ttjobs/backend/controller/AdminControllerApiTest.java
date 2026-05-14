package com.ttjobs.backend.controller;

import com.ttjobs.backend.dto.admin.AdminStatsDTO;
import com.ttjobs.backend.dto.admin.AiMatchEventDTO;
import com.ttjobs.backend.dto.admin.AdminAiMonitoringDTO;
import com.ttjobs.backend.dto.company.CompanyDTO;
import com.ttjobs.backend.dto.company.CompanyVerificationDTO;
import com.ttjobs.backend.dto.job.JobDTO;
import com.ttjobs.backend.dto.common.AdminActionRequest;
import com.ttjobs.backend.entity.Company;
import com.ttjobs.backend.service.AdminService;
import com.ttjobs.backend.service.AiMonitoringService;
import com.ttjobs.backend.service.CompanyVerificationService;
import com.ttjobs.backend.service.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminController.class)
@AutoConfigureMockMvc(addFilters = false)
class AdminControllerApiTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AdminService adminService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private AiMonitoringService aiMonitoringService;

    @MockBean
    private CompanyVerificationService companyVerificationService;

    @Test
    void getStats_shouldReturnExtendedDashboardPayload() throws Exception {
        AdminStatsDTO dto = new AdminStatsDTO();
        dto.setTotalUsers(10);
        dto.setTotalJobs(5);
        dto.setTotalInterviews(3);
        dto.setStoredCandidateMatches(7);
        dto.setAiServiceStatus("ok");
        dto.setAiMatcherReady(true);
        dto.setApplicationStatusCounts(Map.of("submitted", 4L));

        when(adminService.getStats(isNull(), isNull())).thenReturn(dto);

        mockMvc.perform(get("/api/admin/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalUsers").value(10))
                .andExpect(jsonPath("$.totalInterviews").value(3))
                .andExpect(jsonPath("$.storedCandidateMatches").value(7))
                .andExpect(jsonPath("$.aiServiceStatus").value("ok"))
                .andExpect(jsonPath("$.aiMatcherReady").value(true))
                .andExpect(jsonPath("$.applicationStatusCounts.submitted").value(4));
    }

    @Test
    void getAiMonitoring_shouldReturnMonitoringPayload() throws Exception {
        AdminAiMonitoringDTO dto = new AdminAiMonitoringDTO();
        dto.setHealthStatus("ok");
        dto.setMatchClassifierReady(true);
        dto.setEmbeddingMatcherReady(true);
        dto.setRequestCount(12L);
        dto.setFallbackCount(2L);
        dto.setLabelDistribution(Map.of("match", 4L));

        when(aiMonitoringService.getMonitoring()).thenReturn(dto);

        mockMvc.perform(get("/api/admin/ai/monitoring"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.healthStatus").value("ok"))
                .andExpect(jsonPath("$.matchClassifierReady").value(true))
                .andExpect(jsonPath("$.embeddingMatcherReady").value(true))
                .andExpect(jsonPath("$.requestCount").value(12))
                .andExpect(jsonPath("$.fallbackCount").value(2))
                .andExpect(jsonPath("$.labelDistribution.match").value(4));
    }

    @Test
    void getAiTrainingEvents_shouldReturnRecentEvents() throws Exception {
        AiMatchEventDTO event = new AiMatchEventDTO();
        event.setId(1L);
        event.setUserId(10L);
        event.setJobId(20L);
        event.setEventType("recommendation_clicked");
        event.setPredictedLabel("match");
        event.setPredictedScore(91);

        when(aiMonitoringService.getTrainingEvents(eq(5), isNull(), isNull(), isNull(), isNull(), isNull(), isNull()))
                .thenReturn(List.of(event));

        mockMvc.perform(get("/api/admin/ai/training-events?size=5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].eventType").value("recommendation_clicked"))
                .andExpect(jsonPath("$[0].predictedLabel").value("match"))
                .andExpect(jsonPath("$[0].predictedScore").value(91));
    }

    @Test
    void updateCompany_shouldCallAdminService() throws Exception {
        CompanyDTO dto = new CompanyDTO();
        dto.setId(10L);
        dto.setName("TTJobs Co");
        dto.setVerificationStatus("VERIFIED");

        when(adminService.updateCompany(eq(10L), any())).thenReturn(dto);

        mockMvc.perform(put("/api/admin/companies/10")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "TTJobs Co",
                                  "verificationStatus": "VERIFIED",
                                  "reason": "Cap nhat tu admin"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.name").value("TTJobs Co"))
                .andExpect(jsonPath("$.verificationStatus").value("VERIFIED"));
    }

    @Test
    void deleteCompany_shouldCallAdminService() throws Exception {
        mockMvc.perform(delete("/api/admin/companies/10")
                        .contentType(APPLICATION_JSON)
                        .content("{\"reason\":\"Du lieu khong hop le\"}"))
                .andExpect(status().isNoContent());

        verify(adminService).deleteCompany(eq(10L), any());
    }

    @Test
    void verifyCompany_shouldCallVerificationService() throws Exception {
        CompanyVerificationDTO dto = new CompanyVerificationDTO();
        dto.setCompanyId(10L);
        dto.setCompanyName("TTJobs Co");
        dto.setStatus("VERIFIED");

        when(companyVerificationService.review(eq(10L), eq(Company.VerificationStatus.VERIFIED), any(AdminActionRequest.class)))
                .thenReturn(dto);

        mockMvc.perform(post("/api/admin/companies/10/verify")
                        .contentType(APPLICATION_JSON)
                        .content("{\"reason\":\"Ho so hop le\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.companyId").value(10))
                .andExpect(jsonPath("$.status").value("VERIFIED"));
    }

    @Test
    void updateJob_shouldCallAdminService() throws Exception {
        JobDTO dto = new JobDTO();
        dto.setId(20L);
        dto.setTitle("Backend Developer");
        dto.setStatus("open");

        when(adminService.updateJob(eq(20L), any())).thenReturn(dto);

        mockMvc.perform(put("/api/admin/jobs/20")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Backend Developer",
                                  "status": "open",
                                  "currency": "VND"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(20))
                .andExpect(jsonPath("$.title").value("Backend Developer"))
                .andExpect(jsonPath("$.status").value("open"));
    }

    @Test
    void deleteJob_shouldCallAdminService() throws Exception {
        mockMvc.perform(delete("/api/admin/jobs/20")
                        .contentType(APPLICATION_JSON)
                        .content("{\"reason\":\"Tin het hieu luc\"}"))
                .andExpect(status().isNoContent());

        verify(adminService).deleteJob(eq(20L), any());
    }
}

