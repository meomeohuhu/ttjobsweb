package com.ttjobs.backend.controller;

import com.ttjobs.backend.dto.company.CompanyDTO;
import com.ttjobs.backend.dto.company.CompanyFollowStatusDTO;
import com.ttjobs.backend.service.CompanyFollowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/company-follows")
public class CompanyFollowController {

    @Autowired
    private CompanyFollowService companyFollowService;

    @GetMapping("/me")
    public List<CompanyDTO> getMyFollowedCompanies() {
        return companyFollowService.getMyFollowedCompanies();
    }

    @GetMapping("/{companyId}/status")
    public CompanyFollowStatusDTO getFollowStatus(@PathVariable Long companyId) {
        return companyFollowService.getFollowStatus(companyId);
    }

    @PostMapping("/{companyId}")
    public CompanyFollowStatusDTO followCompany(@PathVariable Long companyId) {
        return companyFollowService.followCompany(companyId);
    }

    @DeleteMapping("/{companyId}")
    public CompanyFollowStatusDTO unfollowCompany(@PathVariable Long companyId) {
        return companyFollowService.unfollowCompany(companyId);
    }
}

