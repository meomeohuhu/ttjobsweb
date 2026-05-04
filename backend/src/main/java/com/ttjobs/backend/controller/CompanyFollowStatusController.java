package com.ttjobs.backend.controller;

import com.ttjobs.backend.dto.CompanyFollowStatusDTO;
import com.ttjobs.backend.service.CompanyFollowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/companies")
public class CompanyFollowStatusController {

    @Autowired
    private CompanyFollowService companyFollowService;

    @GetMapping("/{companyId}/follow-status")
    public CompanyFollowStatusDTO getCompanyFollowStatus(@PathVariable Long companyId) {
        return companyFollowService.getFollowStatus(companyId);
    }
}
