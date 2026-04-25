package com.ttjobs.backend.controller;

import com.ttjobs.backend.dto.RecruiterDashboardDTO;
import com.ttjobs.backend.dto.RecruiterApplicationDTO;
import com.ttjobs.backend.dto.RecruiterApplicationDetailDTO;
import com.ttjobs.backend.dto.RecruiterActivityLogDTO;
import com.ttjobs.backend.dto.RecruiterCompanyDTO;
import com.ttjobs.backend.dto.RecruiterJobDTO;
import com.ttjobs.backend.service.RecruiterDashboardService;
import com.ttjobs.backend.service.RecruiterWorkspaceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/recruiter")
public class RecruiterDashboardController {

    @Autowired
    private RecruiterDashboardService recruiterDashboardService;
    @Autowired
    private RecruiterWorkspaceService recruiterWorkspaceService;

    @GetMapping("/dashboard")
    public RecruiterDashboardDTO getDashboard() {
        return recruiterDashboardService.getDashboard();
    }

    @GetMapping("/companies")
    public List<RecruiterCompanyDTO> getManagedCompanies() {
        return recruiterWorkspaceService.getManagedCompanies();
    }

    @GetMapping("/jobs")
    public List<RecruiterJobDTO> getManagedJobs(@RequestParam(required = false) Long companyId,
                                                @RequestParam(required = false) String status,
                                                @RequestParam(required = false) String keyword,
                                                @RequestParam(required = false, defaultValue = "0") Integer page,
                                                @RequestParam(required = false, defaultValue = "50") Integer size) {
        return recruiterWorkspaceService.getManagedJobs(companyId, status, keyword, page, size);
    }

    @GetMapping("/applications")
    public List<RecruiterApplicationDTO> getManagedApplications(@RequestParam(required = false) Long companyId,
                                                                @RequestParam(required = false) Long jobId,
                                                                @RequestParam(required = false) String status,
                                                                @RequestParam(required = false) String keyword,
                                                                @RequestParam(required = false, defaultValue = "0") Integer page,
                                                                @RequestParam(required = false, defaultValue = "100") Integer size) {
        return recruiterWorkspaceService.getManagedApplications(companyId, jobId, status, keyword, page, size);
    }

    @GetMapping("/applications/{id}")
    public RecruiterApplicationDetailDTO getManagedApplicationDetail(@PathVariable Long id) {
        return recruiterWorkspaceService.getManagedApplicationDetail(id);
    }

    @GetMapping("/activity")
    public List<RecruiterActivityLogDTO> getRecentActivities(@RequestParam(required = false, defaultValue = "20") Integer limit) {
        return recruiterDashboardService.getRecentActivities(limit);
    }
}
