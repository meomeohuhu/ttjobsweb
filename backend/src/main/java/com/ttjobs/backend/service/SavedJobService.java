package com.ttjobs.backend.service;

import com.ttjobs.backend.dto.SavedJobDTO;
import com.ttjobs.backend.dto.SavedJobNoteRequest;
import com.ttjobs.backend.entity.Job;
import com.ttjobs.backend.entity.SavedJob;
import com.ttjobs.backend.entity.User;
import com.ttjobs.backend.exception.ResourceNotFoundException;
import com.ttjobs.backend.repository.JobRepository;
import com.ttjobs.backend.repository.SavedJobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
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

        Optional<SavedJob> existing = savedJobRepository.findByUserIdAndJobId(currentUser.getId(), jobId);
        if (existing.isPresent()) {
            return toDto(existing.get());
        }

        Job job = jobRepository.findByIdAndDeletedAtIsNull(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found"));

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

        savedJobRepository.findByUserIdAndJobId(currentUser.getId(), jobId)
                .ifPresent(savedJobRepository::delete);
    }

    public SavedJobDTO updateNote(Long jobId, SavedJobNoteRequest request) {
        User currentUser = authContextService.requireCurrentUser();
        if (currentUser.getRole() != User.Role.CANDIDATE) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only candidate can update saved job notes");
        }

        SavedJob savedJob = savedJobRepository.findByUserIdAndJobId(currentUser.getId(), jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Saved job not found"));

        savedJob.setNote(request.getNote());
        savedJob.setTag(request.getTag());

        return toDto(savedJobRepository.save(savedJob));
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
        dto.setNote(savedJob.getNote());
        dto.setTag(savedJob.getTag());
        if (savedJob.getUser() != null) {
            dto.setUserId(savedJob.getUser().getId());
        }
        if (savedJob.getJob() != null) {
            dto.setJobId(savedJob.getJob().getId());
            dto.setJobTitle(savedJob.getJob().getTitle());
            dto.setJobLocation(savedJob.getJob().getLocation());
            dto.setJobStatus(savedJob.getJob().getStatus());
            dto.setSalary(savedJob.getJob().getSalary());
            dto.setSalaryMin(savedJob.getJob().getSalaryMin());
            dto.setSalaryMax(savedJob.getJob().getSalaryMax());
            dto.setCurrency(savedJob.getJob().getCurrency());
            if (savedJob.getJob().getCompany() != null) {
                dto.setCompanyName(savedJob.getJob().getCompany().getName());
                dto.setCompanyLogoUrl(savedJob.getJob().getCompany().getLogoUrl());
            }
        }
        return dto;
    }
}
