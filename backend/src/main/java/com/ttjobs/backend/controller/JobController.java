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
import java.util.Map;

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
        String effectiveKeyword = (keyword != null && !keyword.isBlank()) ? keyword : q;
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

    @GetMapping("/metadata")
    public Map<String, Object> getJobMetadata() {
        return Map.of(
                "categories", List.of(
                        option("INFORMATION-TECHNOLOGY", "Công nghệ thông tin"),
                        option("SALES", "Kinh doanh - Bán hàng"),
                        option("MARKETING", "Marketing - PR - Quảng cáo"),
                        option("HR", "Nhân sự - Hành chính"),
                        option("FINANCE", "Tài chính - Ngân hàng"),
                        option("CUSTOMER-SERVICE", "Chăm sóc khách hàng"),
                        option("REAL-ESTATE", "Bất động sản"),
                        option("ACCOUNTING", "Kế toán - Kiểm toán - Thuế"),
                        option("DESIGN", "Thiết kế"),
                        option("BUSINESS-DEVELOPMENT", "Phát triển kinh doanh"),
                        option("ENGINEERING", "Kỹ thuật"),
                        option("OPERATIONS", "Vận hành")
                ),
                "jobTypes", List.of(
                        option("Full-time", "Toàn thời gian"),
                        option("Part-time", "Bán thời gian"),
                        option("Hybrid", "Linh hoạt"),
                        option("Remote", "Từ xa"),
                        option("Contract", "Hợp đồng"),
                        option("Internship", "Thực tập")
                ),
                "experienceLevels", List.of(
                        option("ENTRY", "Entry/Fresher"),
                        option("Junior", "Junior"),
                        option("MID", "Middle"),
                        option("Mid", "Middle"),
                        option("SENIOR", "Senior"),
                        option("Senior", "Senior"),
                        option("LEAD", "Lead"),
                        option("Lead", "Lead")
                ),
                "locationsFallback", List.of(
                        locationGroup(1, "Hà Nội", List.of("Ba Đình", "Cầu Giấy", "Đống Đa", "Hai Bà Trưng", "Nam Từ Liêm", "Thanh Xuân")),
                        locationGroup(79, "TP. Hồ Chí Minh", List.of("Quận 1", "Quận 3", "Quận 7", "Bình Thạnh", "Phú Nhuận", "Tân Bình")),
                        locationGroup(74, "Bình Dương", List.of("Thủ Dầu Một", "Dĩ An", "Thuận An", "Bến Cát", "Tân Uyên")),
                        locationGroup(48, "Đà Nẵng", List.of("Hải Châu", "Thanh Khê", "Sơn Trà", "Ngũ Hành Sơn", "Liên Chiểu")),
                        Map.of("code", "remote", "province", "Remote", "districts", List.of("Remote", "Hybrid"))
                )
        );
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
            @RequestParam(required = false, defaultValue = "0") @Min(0) Integer page,
            @RequestParam(required = false, defaultValue = "20") @Min(1) @Max(100) Integer size) {
        String effectiveKeyword = (keyword != null && !keyword.isBlank()) ? keyword : title;
        return jobService.searchJobs(effectiveKeyword, null, location, companyName, jobType, experienceLevel,
                status, salaryMin, salaryMax, skills, "latest", page, size);
    }

    private Map<String, String> option(String value, String label) {
        return Map.of("value", value, "label", label);
    }

    private Map<String, Object> locationGroup(Integer code, String province, List<String> districts) {
        return Map.of("code", code, "province", province, "districts", districts);
    }
}
