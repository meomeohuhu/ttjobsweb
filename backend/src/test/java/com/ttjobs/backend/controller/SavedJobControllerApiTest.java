package com.ttjobs.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ttjobs.backend.dto.SavedJobDTO;
import com.ttjobs.backend.dto.SavedJobNoteRequest;
import com.ttjobs.backend.service.JwtService;
import com.ttjobs.backend.service.SavedJobService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SavedJobController.class)
@AutoConfigureMockMvc(addFilters = false)
class SavedJobControllerApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private SavedJobService savedJobService;
    @MockBean
    private JwtService jwtService;

    @Test
    void getMySavedJobs_shouldReturnList() throws Exception {
        SavedJobDTO dto = new SavedJobDTO();
        dto.setId(1L);
        dto.setJobId(10L);
        dto.setJobTitle("Java Developer");
        dto.setSavedAt(LocalDateTime.now());

        when(savedJobService.getMySavedJobs()).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/saved-jobs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].jobId").value(10));
    }

    @Test
    void saveJob_shouldReturnSavedJob() throws Exception {
        SavedJobDTO dto = new SavedJobDTO();
        dto.setId(2L);
        dto.setJobId(20L);
        dto.setJobTitle("Backend Engineer");

        when(savedJobService.saveJob(20L)).thenReturn(dto);

        mockMvc.perform(post("/api/saved-jobs/20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.jobId").value(20));
    }

    @Test
    void unsaveJob_shouldReturnOk() throws Exception {
        doNothing().when(savedJobService).unsaveJob(eq(30L));

        mockMvc.perform(delete("/api/saved-jobs/30"))
                .andExpect(status().isOk());
    }

    @Test
    void updateNote_shouldReturnUpdatedJob() throws Exception {
        SavedJobNoteRequest request = new SavedJobNoteRequest();
        request.setNote("Test note");
        request.setTag("Test tag");

        SavedJobDTO dto = new SavedJobDTO();
        dto.setId(1L);
        dto.setNote("Test note");
        dto.setTag("Test tag");

        when(savedJobService.updateNote(eq(1L), any(SavedJobNoteRequest.class))).thenReturn(dto);

        mockMvc.perform(patch("/api/saved-jobs/1/note")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.note").value("Test note"))
                .andExpect(jsonPath("$.tag").value("Test tag"));
    }

    @Test
    void updateNote_shouldReturnBadRequest_whenNoteTooLong() throws Exception {
        SavedJobNoteRequest request = new SavedJobNoteRequest();
        request.setNote("a".repeat(2001));

        mockMvc.perform(patch("/api/saved-jobs/1/note")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
