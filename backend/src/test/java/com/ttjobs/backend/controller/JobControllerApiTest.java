package com.ttjobs.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ttjobs.backend.dto.job.JobCategoryStatDTO;
import com.ttjobs.backend.dto.job.JobDTO;
import com.ttjobs.backend.entity.Company;
import com.ttjobs.backend.entity.Job;
import com.ttjobs.backend.service.JobService;
import com.ttjobs.backend.service.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(JobController.class)
@AutoConfigureMockMvc(addFilters = false)
class JobControllerApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private JobService jobService;
    @MockBean
    private JwtService jwtService;

    @Test
    void getJobById_shouldReturnJobDto() throws Exception {
        JobDTO dto = new JobDTO();
        dto.setId(1L);
        dto.setTitle("Java Developer");
        dto.setStatus("open");

        when(jobService.getJobById(1L)).thenReturn(Optional.of(dto));

        mockMvc.perform(get("/api/jobs/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Java Developer"))
                .andExpect(jsonPath("$.status").value("open"));
    }

    @Test
    void searchJobs_shouldReturnList() throws Exception {
        JobDTO dto = new JobDTO();
        dto.setId(2L);
        dto.setTitle("Backend Engineer");
        dto.setStatus("open");

        when(jobService.searchJobs(eq("Backend"), eq(null), eq("HCM"), eq(null), eq("full_time"),
                eq("mid"), eq("open"), eq(null), eq(null), eq(null), eq("latest"), eq(0), eq(20)))
                .thenReturn(List.of(dto));

        mockMvc.perform(get("/api/jobs/search")
                        .param("title", "Backend")
                        .param("location", "HCM")
                        .param("jobType", "full_time")
                        .param("experienceLevel", "mid")
                        .param("status", "open")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(2))
                .andExpect(jsonPath("$[0].title").value("Backend Engineer"));
    }

    @Test
    void getJobs_shouldApplyQueryFilters() throws Exception {
        JobDTO dto = new JobDTO();
        dto.setId(3L);
        dto.setTitle("Filtered Job");
        dto.setStatus("open");

        when(jobService.getPublicJobs(eq("Java"), eq("INFORMATION-TECHNOLOGY"), eq("Ha Noi"),
                eq(null), eq("full_time"), eq("senior"), eq(BigDecimal.valueOf(10000000)),
                eq(BigDecimal.valueOf(30000000)), eq("salary_high"), eq(0), eq(60)))
                .thenReturn(List.of(dto));

        mockMvc.perform(get("/api/jobs")
                        .param("keyword", "Java")
                        .param("category", "INFORMATION-TECHNOLOGY")
                        .param("location", "Ha Noi")
                        .param("jobType", "full_time")
                        .param("experienceLevel", "senior")
                        .param("salaryMin", "10000000")
                        .param("salaryMax", "30000000")
                        .param("sort", "salary_high")
                        .param("page", "0")
                        .param("size", "60"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(3))
                .andExpect(jsonPath("$[0].title").value("Filtered Job"));
    }

    @Test
    void searchJobs_shouldReturnBadRequest_whenSizeOutOfRange() throws Exception {
        mockMvc.perform(get("/api/jobs/search")
                        .param("size", "200"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getHighlightedJobs_shouldReturnList() throws Exception {
        JobDTO dto = new JobDTO();
        dto.setId(5L);
        dto.setTitle("High Salary Job");
        dto.setStatus("open");
        dto.setSavedCount(3L);

        when(jobService.getHighlightedJobs(eq("high_salary"), eq(12))).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/jobs/highlights"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(5))
                .andExpect(jsonPath("$[0].title").value("High Salary Job"))
                .andExpect(jsonPath("$[0].savedCount").value(3));
    }

    @Test
    void getBestJobs_shouldReturnList() throws Exception {
        JobDTO dto = new JobDTO();
        dto.setId(6L);
        dto.setTitle("Most Saved Job");
        dto.setStatus("open");
        dto.setSavedCount(12L);

        when(jobService.getBestJobs(eq("most_saved"), eq(12))).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/jobs/best"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(6))
                .andExpect(jsonPath("$[0].title").value("Most Saved Job"))
                .andExpect(jsonPath("$[0].savedCount").value(12));
    }

    @Test
    void getTopCategories_shouldReturnList() throws Exception {
        when(jobService.getTopCategories(eq(8)))
                .thenReturn(List.of(new JobCategoryStatDTO("SALES", "Kinh doanh - Bán hàng", 5L)));

        mockMvc.perform(get("/api/jobs/categories/top"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].category").value("SALES"))
                .andExpect(jsonPath("$[0].label").value("Kinh doanh - Bán hàng"))
                .andExpect(jsonPath("$[0].jobCount").value(5));
    }

    @Test
    void createJob_shouldReturnSavedJob() throws Exception {
        Job request = new Job();
        request.setTitle("QA Engineer");
        request.setDescription("Automation");
        request.setSalary(BigDecimal.valueOf(1000));
        request.setJobType("full_time");
        request.setExperienceLevel("junior");
        request.setStatus("draft");
        Company company = new Company();
        company.setId(3L);
        request.setCompany(company);

        JobDTO saved = new JobDTO();
        saved.setId(100L);
        saved.setTitle("QA Engineer");
        saved.setStatus("draft");
        saved.setCompanyId(3L);
        saved.setCompanyName("Company 3");

        when(jobService.createJob(any(Job.class))).thenReturn(saved);

        mockMvc.perform(post("/api/jobs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(100))
                .andExpect(jsonPath("$.title").value("QA Engineer"));
    }

    @Test
    void getCompanyJobs_shouldReturnCompanyJobs() throws Exception {
        JobDTO dto = new JobDTO();
        dto.setId(9L);
        dto.setTitle("Company Job");
        dto.setStatus("open");

        when(jobService.getCompanyJobs(3L)).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/jobs/company/3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(9))
                .andExpect(jsonPath("$[0].title").value("Company Job"));
    }
}

