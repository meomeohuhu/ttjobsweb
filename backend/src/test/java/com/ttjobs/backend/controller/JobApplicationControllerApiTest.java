package com.ttjobs.backend.controller;

import com.ttjobs.backend.dto.JobApplicationDTO;
import com.ttjobs.backend.service.JobApplicationService;
import com.ttjobs.backend.service.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(JobApplicationController.class)
@AutoConfigureMockMvc(addFilters = false)
class JobApplicationControllerApiTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JobApplicationService jobApplicationService;
    @MockBean
    private JwtService jwtService;

    @Test
    void getApplicationsByUser_shouldReturnList() throws Exception {
        JobApplicationDTO dto = new JobApplicationDTO();
        dto.setId(10L);
        dto.setStatus("submitted");
        dto.setUserId(1L);
        dto.setJobId(2L);
        dto.setApplicationDate(LocalDateTime.now());

        when(jobApplicationService.getApplicationsByUserId(1L)).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/applications/user/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(10))
                .andExpect(jsonPath("$[0].status").value("submitted"));
    }

    @Test
    void applyForJob_shouldReturnBadRequest_whenMissingJobId() throws Exception {
        mockMvc.perform(multipart("/api/applications/apply"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void applyForJob_shouldReturnApplication() throws Exception {
        JobApplicationDTO app = new JobApplicationDTO();
        app.setId(20L);
        app.setStatus("submitted");

        when(jobApplicationService.applyForJob(eq(2L), isNull(), isNull(), eq(true), eq(false), eq(false), isNull()))
                .thenReturn(app);

        mockMvc.perform(multipart("/api/applications/apply")
                        .param("jobId", "2")
                        .param("useProfileCv", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(20))
                .andExpect(jsonPath("$.status").value("submitted"));
    }

    @Test
    void updateStatus_shouldReturnBadRequest_whenStatusBlank() throws Exception {
        mockMvc.perform(put("/api/applications/20/status")
                        .param("status", " "))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteApplication_shouldReturnOk() throws Exception {
        doNothing().when(jobApplicationService).deleteApplication(eq(20L));

        mockMvc.perform(delete("/api/applications/20"))
                .andExpect(status().isOk());
    }

    @Test
    void getApplicationsForMyJobs_shouldReturnList() throws Exception {
        JobApplicationDTO dto = new JobApplicationDTO();
        dto.setId(30L);
        dto.setStatus("reviewing");
        dto.setJobId(99L);

        when(jobApplicationService.getApplicationsForMyJobs()).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/applications/recruiter/my-jobs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(30))
                .andExpect(jsonPath("$[0].status").value("reviewing"));
    }

    @Test
    void withdrawApplication_shouldReturnUpdatedApplication() throws Exception {
        JobApplicationDTO app = new JobApplicationDTO();
        app.setId(44L);
        app.setStatus("withdrawn");

        when(jobApplicationService.withdrawApplication(44L)).thenReturn(app);

        mockMvc.perform(put("/api/applications/44/withdraw"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(44))
                .andExpect(jsonPath("$.status").value("withdrawn"));
    }

    @Test
    void streamCv_shouldReturnOk() throws Exception {
        doNothing().when(jobApplicationService).streamCv(eq(10L), org.mockito.ArgumentMatchers.any());

        mockMvc.perform(get("/api/applications/10/cv-stream"))
                .andExpect(status().isOk());
    }
}
