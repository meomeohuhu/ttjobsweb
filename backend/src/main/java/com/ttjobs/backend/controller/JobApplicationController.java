package com.ttjobs.backend.controller;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.ttjobs.backend.dto.JobApplicationDTO;
import com.ttjobs.backend.service.JobApplicationService;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@RestController
@RequestMapping("/api/applications")
@Validated
public class JobApplicationController {

    @Autowired
    private JobApplicationService jobApplicationService;

    @GetMapping
    public List<JobApplicationDTO> getAllApplications() {
        return jobApplicationService.getAllApplications();
    }

    @GetMapping("/user/{userId}")
    public List<JobApplicationDTO> getApplicationsByUserId(@PathVariable Long userId) {
        return jobApplicationService.getApplicationsByUserId(userId);
    }

    @GetMapping("/job/{jobId}")
    public List<JobApplicationDTO> getApplicationsByJobId(@PathVariable Long jobId) {
        return jobApplicationService.getApplicationsByJobId(jobId);
    }

    @GetMapping("/recruiter/my-jobs")
    public List<JobApplicationDTO> getApplicationsForMyJobs() {
        return jobApplicationService.getApplicationsForMyJobs();
    }

    @PostMapping("/apply")
    public JobApplicationDTO applyForJob(@RequestParam @NotNull Long userId, @RequestParam @NotNull Long jobId) {
        return jobApplicationService.applyForJob(userId, jobId);
    }

    @PutMapping("/{applicationId}/status")
    public JobApplicationDTO updateApplicationStatus(@PathVariable Long applicationId, @RequestParam @NotBlank String status) {
        return jobApplicationService.updateApplicationStatus(applicationId, status);
    }

    @PutMapping("/{applicationId}/withdraw")
    public JobApplicationDTO withdrawApplication(@PathVariable Long applicationId) {
        return jobApplicationService.withdrawApplication(applicationId);
    }

    // Keep old DELETE endpoint contract, but service performs candidate withdraw.
    @DeleteMapping("/{applicationId}")
    public void deleteApplication(@PathVariable Long applicationId) {
        jobApplicationService.deleteApplication(applicationId);
    }

    @GetMapping("/{applicationId}/timeline")
    public List<com.ttjobs.backend.dto.ApplicationTimelineDTO> getTimeline(@PathVariable Long applicationId) {
        return jobApplicationService.getApplicationTimeline(applicationId);
    }
}
