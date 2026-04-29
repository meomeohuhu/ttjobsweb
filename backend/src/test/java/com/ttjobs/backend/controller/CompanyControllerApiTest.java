package com.ttjobs.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ttjobs.backend.dto.CompanyDTO;
import com.ttjobs.backend.dto.CompanyMemberDTO;
import com.ttjobs.backend.dto.CompanyMemberUpsertRequest;
import com.ttjobs.backend.dto.CompanyPublicPageDTO;
import com.ttjobs.backend.dto.JobDTO;
import com.ttjobs.backend.service.CompanyService;
import com.ttjobs.backend.service.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CompanyController.class)
@AutoConfigureMockMvc(addFilters = false)
class CompanyControllerApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CompanyService companyService;
    @MockBean
    private JwtService jwtService;

    @Test
    void getTopCompaniesBySavedJobs_shouldReturnList() throws Exception {
        CompanyDTO dto = new CompanyDTO();
        dto.setId(7L);
        dto.setName("Acme");
        dto.setJobCount(12L);
        dto.setSavedJobCount(34L);

        when(companyService.getTopCompaniesBySavedJobs(2)).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/companies/top-saved-jobs?limit=2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(7))
                .andExpect(jsonPath("$[0].name").value("Acme"))
                .andExpect(jsonPath("$[0].savedJobCount").value(34));
    }

    @Test
    void getPublicCompanyJobs_shouldReturnList() throws Exception {
        JobDTO dto = new JobDTO();
        dto.setId(100L);
        dto.setTitle("Java Developer");
        dto.setCompanyName("Acme");

        when(companyService.getPublicCompanyJobs(3L)).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/companies/3/jobs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(100))
                .andExpect(jsonPath("$[0].title").value("Java Developer"))
                .andExpect(jsonPath("$[0].companyName").value("Acme"));
    }

    @Test
    void getPublicCompanyPage_shouldReturnPayload() throws Exception {
        CompanyDTO company = new CompanyDTO();
        company.setId(7L);
        company.setName("Acme");
        company.setSavedJobCount(34L);

        JobDTO job = new JobDTO();
        job.setId(100L);
        job.setTitle("Java Developer");

        CompanyPublicPageDTO payload = new CompanyPublicPageDTO();
        payload.setCompany(company);
        payload.setJobs(List.of(job));

        when(companyService.getPublicCompanyPage(7L)).thenReturn(payload);

        mockMvc.perform(get("/api/companies/7/public-page"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.company.id").value(7))
                .andExpect(jsonPath("$.company.name").value("Acme"))
                .andExpect(jsonPath("$.jobs[0].title").value("Java Developer"));
    }

    @Test
    void getCompanyMembers_shouldReturnList() throws Exception {
        CompanyMemberDTO dto = new CompanyMemberDTO();
        dto.setId(10L);
        dto.setCompanyId(1L);
        dto.setUserId(2L);
        dto.setMemberRole("ADMIN");

        when(companyService.getCompanyMembers(1L)).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/companies/1/members"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(10))
                .andExpect(jsonPath("$[0].memberRole").value("ADMIN"));
    }

    @Test
    void addCompanyMember_shouldReturnCreatedMember() throws Exception {
        CompanyMemberUpsertRequest req = new CompanyMemberUpsertRequest();
        req.setUserId(2L);
        req.setMemberRole("RECRUITER");

        CompanyMemberDTO dto = new CompanyMemberDTO();
        dto.setId(11L);
        dto.setCompanyId(1L);
        dto.setUserId(2L);
        dto.setMemberRole("RECRUITER");

        when(companyService.addCompanyMember(eq(1L), any(CompanyMemberUpsertRequest.class))).thenReturn(dto);

        mockMvc.perform(post("/api/companies/1/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(11))
                .andExpect(jsonPath("$.memberRole").value("RECRUITER"));
    }

    @Test
    void updateCompanyMember_shouldReturnUpdatedMember() throws Exception {
        CompanyMemberUpsertRequest req = new CompanyMemberUpsertRequest();
        req.setUserId(2L);
        req.setMemberRole("ADMIN");

        CompanyMemberDTO dto = new CompanyMemberDTO();
        dto.setId(11L);
        dto.setCompanyId(1L);
        dto.setUserId(2L);
        dto.setMemberRole("ADMIN");

        when(companyService.updateCompanyMember(eq(1L), eq(11L), any(CompanyMemberUpsertRequest.class))).thenReturn(dto);

        mockMvc.perform(put("/api/companies/1/members/11")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.memberRole").value("ADMIN"));
    }

    @Test
    void removeCompanyMember_shouldReturnOk() throws Exception {
        doNothing().when(companyService).removeCompanyMember(1L, 11L);

        mockMvc.perform(delete("/api/companies/1/members/11"))
                .andExpect(status().isOk());
    }

    @Test
    void addCompanyMember_shouldReturnBadRequest_whenMemberRoleMissing() throws Exception {
        CompanyMemberUpsertRequest req = new CompanyMemberUpsertRequest();
        req.setUserId(2L);

        mockMvc.perform(post("/api/companies/1/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }
}
