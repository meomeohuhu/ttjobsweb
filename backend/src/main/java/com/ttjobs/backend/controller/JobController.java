package com.ttjobs.backend.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.ttjobs.backend.entity.Job;
import com.ttjobs.backend.dto.JobCategoryStatDTO;
import com.ttjobs.backend.dto.JobDTO;
import com.ttjobs.backend.service.JobService;
import com.ttjobs.backend.exception.ResourceNotFoundException;
import org.springframework.validation.annotation.Validated;
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/jobs")
@Validated
public class JobController {

    @Autowired
    private JobService jobService;

    @GetMapping
    public List<JobDTO> getAllJobs(@RequestParam(required = false) String keyword,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String companyName,
            @RequestParam(required = false) String jobType,
            @RequestParam(required = false) String experienceLevel,
            @RequestParam(required = false) BigDecimal salaryMin,
            @RequestParam(required = false) BigDecimal salaryMax,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) @Min(0) Integer page,
            @RequestParam(required = false) @Min(1) @Max(100) Integer size) {
        String effectiveKeyword = (keyword != null && !keyword.isBlank()) ? keyword.trim() : (q != null ? q.trim() : null);
        return jobService.getPublicJobs(effectiveKeyword, category, location, companyName, jobType, experienceLevel,
                salaryMin, salaryMax, sort, page, size);
    }

    @GetMapping("/highlights")
    public List<JobDTO> getHighlightedJobs(@RequestParam(required = false, defaultValue = "high_salary") String type,
            @RequestParam(required = false, defaultValue = "12") @Min(1) Integer size) {
        return jobService.getHighlightedJobs(type, size);
    }

    @GetMapping("/best")
    public List<JobDTO> getBestJobs(@RequestParam(required = false, defaultValue = "most_saved") String type,
            @RequestParam(required = false, defaultValue = "12") @Min(1) Integer size) {
        return jobService.getBestJobs(type, size);
    }

    @GetMapping("/categories/top")
    public List<JobCategoryStatDTO> getTopCategories(
            @RequestParam(required = false, defaultValue = "8") @Min(1) Integer size) {
        return jobService.getTopCategories(size);
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

    @PostMapping(value = "/{id}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public JobDTO uploadJobImage(@PathVariable Long id, @RequestPart("file") MultipartFile file) {
        return jobService.uploadJobImage(id, file);
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
            @RequestParam(required = false) String sort,
            @RequestParam(required = false, defaultValue = "0") @Min(0) Integer page,
            @RequestParam(required = false, defaultValue = "20") @Min(1) @Max(100) Integer size) {
        String effectiveKeyword = (keyword != null && !keyword.isBlank()) ? keyword.trim() : (title != null ? title.trim() : null);
        String effectiveSort = (sort != null && !sort.isBlank()) ? sort.trim() : "latest";
        return jobService.searchJobs(effectiveKeyword, null, location, companyName, jobType, experienceLevel,
                status, salaryMin, salaryMax, skills, effectiveSort, page, size);
    }
}
