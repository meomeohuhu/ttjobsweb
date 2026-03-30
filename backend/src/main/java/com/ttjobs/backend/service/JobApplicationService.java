package com.ttjobs.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ttjobs.backend.entity.JobApplication;
import com.ttjobs.backend.entity.User;
import com.ttjobs.backend.entity.Job;
import com.ttjobs.backend.dto.JobApplicationDTO;
import com.ttjobs.backend.repository.JobApplicationRepository;
import com.ttjobs.backend.repository.UserRepository;
import com.ttjobs.backend.repository.JobRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class JobApplicationService {

    @Autowired
    private JobApplicationRepository jobApplicationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JobRepository jobRepository;

    public List<JobApplicationDTO> getAllApplications() {
        return jobApplicationRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<JobApplicationDTO> getApplicationsByUserId(Long userId) {
        return jobApplicationRepository.findByUserId(userId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<JobApplicationDTO> getApplicationsByJobId(Long jobId) {
        return jobApplicationRepository.findByJobId(jobId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public JobApplication applyForJob(Long userId, Long jobId) {
        // Check if user already applied
        Optional<JobApplication> existingApplication = jobApplicationRepository.findByUserIdAndJobId(userId, jobId);
        if (existingApplication.isPresent()) {
            throw new RuntimeException("User has already applied for this job");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Job not found"));

        JobApplication application = new JobApplication();
        application.setUser(user);
        application.setJob(job);
        application.setApplicationDate(LocalDateTime.now());
        application.setStatus("PENDING");

        return jobApplicationRepository.save(application);
    }

    public JobApplication updateApplicationStatus(Long applicationId, String status) {
        JobApplication application = jobApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Application not found"));
        application.setStatus(status);
        return jobApplicationRepository.save(application);
    }

    public void deleteApplication(Long applicationId) {
        jobApplicationRepository.deleteById(applicationId);
    }

    private JobApplicationDTO convertToDTO(JobApplication application) {
        JobApplicationDTO dto = new JobApplicationDTO();
        dto.setId(application.getId());
        dto.setApplicationDate(application.getApplicationDate());
        dto.setStatus(application.getStatus());
        if (application.getUser() != null) {
            dto.setUserId(application.getUser().getId());
            dto.setUserName(application.getUser().getName());
        }
        if (application.getJob() != null) {
            dto.setJobId(application.getJob().getId());
            dto.setJobTitle(application.getJob().getTitle());
            if (application.getJob().getCompany() != null) {
                dto.setCompanyName(application.getJob().getCompany().getName());
            }
        }
        return dto;
    }
}