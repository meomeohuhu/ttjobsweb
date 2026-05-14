package com.ttjobs.backend.controller;

import com.ttjobs.backend.dto.BulkApplicationStatusRequest;
import com.ttjobs.backend.dto.CandidateSearchDTO;
import com.ttjobs.backend.dto.InterviewScheduleDTO;
import com.ttjobs.backend.dto.InterviewScheduleRequest;
import com.ttjobs.backend.dto.RecruiterDashboardDTO;
import com.ttjobs.backend.dto.RecruiterApplicationDTO;
import com.ttjobs.backend.dto.RecruiterApplicationDetailDTO;
import com.ttjobs.backend.dto.RecruiterActivityLogDTO;
import com.ttjobs.backend.dto.RecruiterCompanyDTO;
import com.ttjobs.backend.dto.RecruiterJobDTO;
import com.ttjobs.backend.dto.RecruiterWorkspaceDTO;
import com.ttjobs.backend.dto.RecruiterReportDTO;
import com.ttjobs.backend.dto.RecruitmentCampaignDTO;
import com.ttjobs.backend.dto.RecruitmentCampaignRequest;
import com.ttjobs.backend.service.RecruiterDashboardService;
import com.ttjobs.backend.service.RecruiterWorkspaceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
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

    @GetMapping("/workspace")
    public RecruiterWorkspaceDTO getWorkspaceSummary() {
        return recruiterWorkspaceService.getWorkspaceSummary();
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

    @PutMapping("/applications/status")
    public List<RecruiterApplicationDTO> bulkUpdateApplicationStatus(@RequestBody BulkApplicationStatusRequest request) {
        return recruiterWorkspaceService.bulkUpdateApplicationStatus(request.getApplicationIds(), request.getStatus());
    }

    @GetMapping("/candidates/search")
    public List<CandidateSearchDTO> searchCandidates(@RequestParam(required = false) String keyword,
                                                     @RequestParam(required = false) Integer minExperience,
                                                     @RequestParam(required = false) String status) {
        return recruiterWorkspaceService.searchCandidates(keyword, minExperience, status);
    }

    @GetMapping("/reports")
    public RecruiterReportDTO getReport(@RequestParam(required = false, defaultValue = "30") Integer days) {
        return recruiterWorkspaceService.getReport(days);
    }

    @GetMapping("/interviews")
    public List<InterviewScheduleDTO> getInterviews() {
        return recruiterWorkspaceService.getManagedInterviews();
    }

    @PostMapping("/interviews")
    public InterviewScheduleDTO createInterview(@RequestBody InterviewScheduleRequest request) {
        return recruiterWorkspaceService.createInterview(request);
    }

    @PutMapping("/interviews/{id}/status")
    public InterviewScheduleDTO updateInterviewStatus(@PathVariable Long id, @RequestParam String status) {
        return recruiterWorkspaceService.updateInterviewStatus(id, status);
    }

    @GetMapping("/campaigns")
    public List<RecruitmentCampaignDTO> getCampaigns() {
        return recruiterWorkspaceService.getCampaigns();
    }

    @PostMapping("/campaigns")
    public RecruitmentCampaignDTO createCampaign(@RequestBody RecruitmentCampaignRequest request) {
        return recruiterWorkspaceService.saveCampaign(null, request);
    }

    @PutMapping("/campaigns/{id}")
    public RecruitmentCampaignDTO updateCampaign(@PathVariable Long id, @RequestBody RecruitmentCampaignRequest request) {
        return recruiterWorkspaceService.saveCampaign(id, request);
    }

    @GetMapping("/activity")
    public List<RecruiterActivityLogDTO> getRecentActivities(@RequestParam(required = false, defaultValue = "20") Integer limit) {
        return recruiterDashboardService.getRecentActivities(limit);
    }
}
