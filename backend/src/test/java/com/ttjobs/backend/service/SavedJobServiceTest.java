package com.ttjobs.backend.service;

import com.ttjobs.backend.dto.SavedJobDTO;
import com.ttjobs.backend.entity.Company;
import com.ttjobs.backend.entity.Job;
import com.ttjobs.backend.entity.SavedJob;
import com.ttjobs.backend.entity.User;
import com.ttjobs.backend.repository.JobRepository;
import com.ttjobs.backend.repository.SavedJobRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SavedJobServiceTest {

    @Mock
    private SavedJobRepository savedJobRepository;
    @Mock
    private JobRepository jobRepository;
    @Mock
    private AuthContextService authContextService;

    @InjectMocks
    private SavedJobService savedJobService;

    @Test
    void saveJob_shouldReturnExisting_whenDuplicate() {
        User candidate = user(1L, User.Role.CANDIDATE);
        SavedJob existing = new SavedJob();
        existing.setId(100L);
        existing.setUser(candidate);
        existing.setJob(job(10L, "Java", "open"));

        when(authContextService.requireCurrentUser()).thenReturn(candidate);
        when(savedJobRepository.findByUserIdAndJobId(1L, 10L)).thenReturn(Optional.of(existing));

        SavedJobDTO result = savedJobService.saveJob(10L);
        assertEquals(100L, result.getId());
    }

    @Test
    void saveJob_shouldReturnSavedDto_whenValid() {
        User candidate = user(1L, User.Role.CANDIDATE);
        Job job = job(10L, "Java Developer", "open");

        SavedJob savedJob = new SavedJob();
        savedJob.setId(100L);
        savedJob.setUser(candidate);
        savedJob.setJob(job);
        savedJob.setSavedAt(LocalDateTime.now());

        when(authContextService.requireCurrentUser()).thenReturn(candidate);
        when(savedJobRepository.findByUserIdAndJobId(1L, 10L)).thenReturn(Optional.empty());
        when(jobRepository.findByIdAndDeletedAtIsNull(10L)).thenReturn(Optional.of(job));
        when(savedJobRepository.save(any(SavedJob.class))).thenReturn(savedJob);

        SavedJobDTO result = savedJobService.saveJob(10L);
        assertEquals(100L, result.getId());
        assertEquals(10L, result.getJobId());
        assertEquals("Java Developer", result.getJobTitle());
    }

    @Test
    void unsaveJob_shouldDelete_whenOwnSavedJobExists() {
        User candidate = user(1L, User.Role.CANDIDATE);
        SavedJob savedJob = new SavedJob();
        savedJob.setId(20L);
        savedJob.setUser(candidate);
        savedJob.setJob(job(10L, "Backend", "open"));

        when(authContextService.requireCurrentUser()).thenReturn(candidate);
        when(savedJobRepository.findByUserIdAndJobId(1L, 10L)).thenReturn(Optional.of(savedJob));

        savedJobService.unsaveJob(10L);
        verify(savedJobRepository).delete(savedJob);
    }

    @Test
    void unsaveJob_shouldReturnSilently_whenNotExists() {
        User candidate = user(1L, User.Role.CANDIDATE);
        when(authContextService.requireCurrentUser()).thenReturn(candidate);
        when(savedJobRepository.findByUserIdAndJobId(1L, 10L)).thenReturn(Optional.empty());

        savedJobService.unsaveJob(10L);
        // No exception thrown, no delete called
    }

    @Test
    void getMySavedJobs_shouldReturnListForCandidate() {
        User candidate = user(1L, User.Role.CANDIDATE);
        SavedJob savedJob = new SavedJob();
        savedJob.setId(30L);
        savedJob.setUser(candidate);
        savedJob.setJob(job(11L, "Spring Boot", "open"));
        savedJob.setSavedAt(LocalDateTime.now());

        when(authContextService.requireCurrentUser()).thenReturn(candidate);
        when(savedJobRepository.findByUserIdOrderBySavedAtDesc(1L)).thenReturn(List.of(savedJob));

        List<SavedJobDTO> result = savedJobService.getMySavedJobs();
        assertEquals(1, result.size());
        assertEquals(11L, result.get(0).getJobId());
    }

    @Test
    void updateNote_shouldUpdateAndReturnDto() {
        User candidate = user(1L, User.Role.CANDIDATE);
        SavedJob savedJob = new SavedJob();
        savedJob.setId(100L);
        savedJob.setUser(candidate);
        savedJob.setJob(job(10L, "Java", "open"));

        com.ttjobs.backend.dto.SavedJobNoteRequest request = new com.ttjobs.backend.dto.SavedJobNoteRequest();
        request.setNote("New note");
        request.setTag("Important");

        when(authContextService.requireCurrentUser()).thenReturn(candidate);
        when(savedJobRepository.findByUserIdAndJobId(1L, 10L)).thenReturn(Optional.of(savedJob));
        when(savedJobRepository.save(any(SavedJob.class))).thenAnswer(invocation -> invocation.getArgument(0));

        SavedJobDTO result = savedJobService.updateNote(10L, request);
        assertEquals("New note", result.getNote());
        assertEquals("Important", result.getTag());
    }

    @Test
    void saveJob_shouldReturnForbidden_whenRecruiterCalls() {
        User recruiter = user(2L, User.Role.RECRUITER);
        when(authContextService.requireCurrentUser()).thenReturn(recruiter);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> savedJobService.saveJob(10L));

        assertEquals(403, ex.getStatusCode().value());
    }

    private User user(Long id, User.Role role) {
        User user = new User();
        user.setId(id);
        user.setRole(role);
        user.setEmail("u" + id + "@mail.com");
        return user;
    }

    private Job job(Long id, String title, String status) {
        Job job = new Job();
        job.setId(id);
        job.setTitle(title);
        job.setStatus(status);
        Company company = new Company();
        company.setId(1L);
        company.setName("TT");
        job.setCompany(company);
        return job;
    }
}
