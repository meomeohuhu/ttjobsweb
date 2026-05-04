package com.ttjobs.backend.controller;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.ttjobs.backend.dto.JobApplicationDTO;
import com.ttjobs.backend.service.JobApplicationService;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.multipart.MultipartFile;

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

    @GetMapping("/me")
    public List<JobApplicationDTO> getMyApplications() {
        return jobApplicationService.getMyApplications();
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

    @PostMapping(value = "/apply", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public JobApplicationDTO applyForJob(
            @RequestParam @NotNull Long jobId,
            @RequestParam(required = false) MultipartFile file,
            @RequestParam(required = false) Long cvId,
            @RequestParam(defaultValue = "false") boolean useProfileCv,
            @RequestParam(defaultValue = "false") boolean useSystemCv,
            @RequestParam(defaultValue = "false") boolean saveToCvList,
            @RequestParam(required = false) String coverLetter
    ) {
        return jobApplicationService.applyForJob(jobId, file, cvId, useProfileCv, useSystemCv, saveToCvList, coverLetter);
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

    @GetMapping("/{applicationId}/cv-stream")
    public void streamCv(@PathVariable Long applicationId, HttpServletResponse response) {
        jobApplicationService.streamCv(applicationId, response);
    }
}
