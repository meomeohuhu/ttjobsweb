package com.ttjobs.backend.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.ttjobs.backend.entity.Job;
import com.ttjobs.backend.dto.JobDTO;
import com.ttjobs.backend.service.JobService;
import com.ttjobs.backend.exception.ResourceNotFoundException;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/jobs")
@Validated
public class JobController {

    @Autowired
    private JobService jobService;

    @GetMapping
    public List<JobDTO> getAllJobs() {
        return jobService.getAllJobs();
    }

    @GetMapping("/{id}")
    public JobDTO getJobById(@PathVariable Long id) {
        return jobService.getJobById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found"));
    }

    @GetMapping("/company/{companyId}")
    public List<JobDTO> getCompanyJobs(@PathVariable Long companyId) {
        return jobService.getCompanyJobs(companyId);
    }

    @PostMapping
    public JobDTO createJob(@Valid @RequestBody Job job) {
        return jobService.createJob(job);
    }

    @PutMapping("/{id}")
    public JobDTO updateJob(@PathVariable Long id, @Valid @RequestBody Job job) {
        return jobService.updateJob(id, job);
    }

    // Keep old DELETE endpoint contract, but service performs soft close.
    @DeleteMapping("/{id}")
    public void deleteJob(@PathVariable Long id) {
        jobService.deleteJob(id);
    }

    @PutMapping("/{id}/close")
    public JobDTO closeJob(@PathVariable Long id) {
        Job closeRequest = new Job();
        closeRequest.setStatus("closed");
        return jobService.updateJob(id, closeRequest);
    }

    @GetMapping("/search")
    public List<JobDTO> searchJobs(@RequestParam(required = false) String keyword,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String companyName,
            @RequestParam(required = false) String jobType,
            @RequestParam(required = false) String experienceLevel,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) BigDecimal salaryMin,
            @RequestParam(required = false) BigDecimal salaryMax,
            @RequestParam(required = false) List<String> skills,
            @RequestParam(required = false, defaultValue = "0") @Min(0) Integer page,
            @RequestParam(required = false, defaultValue = "20") @Min(1) @Max(100) Integer size) {
        String effectiveKeyword = (keyword != null && !keyword.isBlank()) ? keyword : title;
        return jobService.searchJobs(effectiveKeyword, location, companyName, jobType, experienceLevel,
                status, salaryMin, salaryMax, skills, page, size);
    }
}
