package com.ttjobs.backend.service;

import com.ttjobs.backend.dto.SavedJobDTO;
import com.ttjobs.backend.entity.Job;
import com.ttjobs.backend.entity.SavedJob;
import com.ttjobs.backend.entity.User;
import com.ttjobs.backend.repository.JobRepository;
import com.ttjobs.backend.repository.SavedJobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SavedJobService {

    @Autowired
    private SavedJobRepository savedJobRepository;

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private AuthContextService authContextService;

    public SavedJobDTO saveJob(Long jobId) {
        User currentUser = authContextService.requireCurrentUser();
        if (currentUser.getRole() != User.Role.CANDIDATE) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only candidate can save jobs");
        }

        if (savedJobRepository.existsByUserIdAndJobId(currentUser.getId(), jobId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Job already saved");
        }

        Job job = jobRepository.findByIdAndDeletedAtIsNull(jobId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Job not found"));

        SavedJob savedJob = new SavedJob();
        savedJob.setUser(currentUser);
        savedJob.setJob(job);

        return toDto(savedJobRepository.save(savedJob));
    }

    public void unsaveJob(Long jobId) {
        User currentUser = authContextService.requireCurrentUser();
        if (currentUser.getRole() != User.Role.CANDIDATE) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only candidate can unsave jobs");
        }

        SavedJob savedJob = savedJobRepository.findByUserIdAndJobId(currentUser.getId(), jobId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Saved job not found"));

        savedJobRepository.delete(savedJob);
    }

    public List<SavedJobDTO> getMySavedJobs() {
        User currentUser = authContextService.requireCurrentUser();
        if (currentUser.getRole() != User.Role.CANDIDATE) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only candidate can view saved jobs");
        }

        return savedJobRepository.findByUserIdOrderBySavedAtDesc(currentUser.getId())
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    private SavedJobDTO toDto(SavedJob savedJob) {
        SavedJobDTO dto = new SavedJobDTO();
        dto.setId(savedJob.getId());
        dto.setSavedAt(savedJob.getSavedAt());
        if (savedJob.getUser() != null) {
            dto.setUserId(savedJob.getUser().getId());
        }
        if (savedJob.getJob() != null) {
            dto.setJobId(savedJob.getJob().getId());
            dto.setJobTitle(savedJob.getJob().getTitle());
            dto.setJobLocation(savedJob.getJob().getLocation());
            dto.setJobStatus(savedJob.getJob().getStatus());
            if (savedJob.getJob().getCompany() != null) {
                dto.setCompanyName(savedJob.getJob().getCompany().getName());
            }
        }
        return dto;
    }
}
