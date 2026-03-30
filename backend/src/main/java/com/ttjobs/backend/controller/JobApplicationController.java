package com.ttjobs.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.ttjobs.backend.entity.JobApplication;
import com.ttjobs.backend.dto.JobApplicationDTO;
import com.ttjobs.backend.service.JobApplicationService;
import java.util.List;

@RestController
@RequestMapping("/api/applications")
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

    @PostMapping("/apply")
    public JobApplication applyForJob(@RequestParam Long userId, @RequestParam Long jobId) {
        return jobApplicationService.applyForJob(userId, jobId);
    }

    @PutMapping("/{applicationId}/status")
    public JobApplication updateApplicationStatus(@PathVariable Long applicationId, @RequestParam String status) {
        return jobApplicationService.updateApplicationStatus(applicationId, status);
    }

    @DeleteMapping("/{applicationId}")
    public void deleteApplication(@PathVariable Long applicationId) {
        jobApplicationService.deleteApplication(applicationId);
    }
}