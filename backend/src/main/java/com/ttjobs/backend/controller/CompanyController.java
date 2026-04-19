package com.ttjobs.backend.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.ttjobs.backend.entity.Company;
import com.ttjobs.backend.dto.CompanyDTO;
import com.ttjobs.backend.dto.CompanyMemberDTO;
import com.ttjobs.backend.dto.CompanyMemberUpsertRequest;
import com.ttjobs.backend.dto.CompanyPublicPageDTO;
import com.ttjobs.backend.dto.JobDTO;
import com.ttjobs.backend.service.CompanyService;
import java.util.List;

@RestController
@RequestMapping("/api/companies")
public class CompanyController {

    @Autowired
    private CompanyService companyService;

    @GetMapping
    public List<CompanyDTO> getAllCompanies() {
        return companyService.getAllCompanies();
    }

    @GetMapping("/top-saved-jobs")
    public List<CompanyDTO> getTopCompaniesBySavedJobs(@RequestParam(defaultValue = "6") int limit) {
        return companyService.getTopCompaniesBySavedJobs(limit);
    }

    @GetMapping("/{id}")
    public CompanyDTO getCompanyById(@PathVariable Long id) {
        return companyService.getCompanyById(id)
                .orElseThrow(() -> new RuntimeException("Company not found"));
    }

    @GetMapping("/{companyId}/jobs")
    public List<JobDTO> getPublicCompanyJobs(@PathVariable Long companyId) {
        return companyService.getPublicCompanyJobs(companyId);
    }

    @GetMapping("/{companyId}/public-page")
    public CompanyPublicPageDTO getPublicCompanyPage(@PathVariable Long companyId) {
        return companyService.getPublicCompanyPage(companyId);
    }

    @PostMapping
    public CompanyDTO createCompany(@RequestBody Company company) {
        return companyService.createCompany(company);
    }

    @PutMapping("/{id}")
    public CompanyDTO updateCompany(@PathVariable Long id, @RequestBody Company company) {
        return companyService.updateCompany(id, company);
    }

    @DeleteMapping("/{id}")
    public void deleteCompany(@PathVariable Long id) {
        companyService.deleteCompany(id);
    }

    @GetMapping("/{companyId}/members")
    public List<CompanyMemberDTO> getCompanyMembers(@PathVariable Long companyId) {
        return companyService.getCompanyMembers(companyId);
    }

    @PostMapping("/{companyId}/members")
    public CompanyMemberDTO addCompanyMember(@PathVariable Long companyId,
                                             @Valid @RequestBody CompanyMemberUpsertRequest request) {
        return companyService.addCompanyMember(companyId, request);
    }

    @PutMapping("/{companyId}/members/{memberId}")
    public CompanyMemberDTO updateCompanyMember(@PathVariable Long companyId,
                                                @PathVariable Long memberId,
                                                @Valid @RequestBody CompanyMemberUpsertRequest request) {
        return companyService.updateCompanyMember(companyId, memberId, request);
    }

    @DeleteMapping("/{companyId}/members/{memberId}")
    public void removeCompanyMember(@PathVariable Long companyId, @PathVariable Long memberId) {
        companyService.removeCompanyMember(companyId, memberId);
    }
}
