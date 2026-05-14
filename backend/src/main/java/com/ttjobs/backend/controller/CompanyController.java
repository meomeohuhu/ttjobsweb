package com.ttjobs.backend.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.ttjobs.backend.entity.Company;
import com.ttjobs.backend.dto.company.CompanyDTO;
import com.ttjobs.backend.dto.company.CompanyMemberDTO;
import com.ttjobs.backend.dto.company.CompanyMemberUpsertRequest;
import com.ttjobs.backend.dto.company.CompanyPublicPageDTO;
import com.ttjobs.backend.dto.company.CompanyVerificationDTO;
import com.ttjobs.backend.dto.company.CompanyVerificationRequest;
import com.ttjobs.backend.dto.job.JobDTO;
import com.ttjobs.backend.service.CompanyService;
import com.ttjobs.backend.service.CompanyVerificationService;
import java.util.List;

@RestController
@RequestMapping("/api/companies")
public class CompanyController {

    @Autowired
    private CompanyService companyService;
    @Autowired(required = false)
    private CompanyVerificationService companyVerificationService;

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

    @PostMapping(value = "/{id}/logo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public CompanyDTO uploadCompanyLogo(@PathVariable Long id, @RequestPart("file") MultipartFile file) {
        return companyService.uploadCompanyLogo(id, file);
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

    @GetMapping("/{companyId}/verification")
    public CompanyVerificationDTO getVerification(@PathVariable Long companyId) {
        return companyVerificationService.getMyVerification(companyId);
    }

    @PutMapping("/{companyId}/verification")
    public CompanyVerificationDTO updateVerification(@PathVariable Long companyId,
                                                     @RequestBody CompanyVerificationRequest request) {
        return companyVerificationService.upsertVerification(companyId, request);
    }
}

