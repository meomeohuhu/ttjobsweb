package com.ttjobs.backend.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.ttjobs.backend.dto.CompanyDTO;
import com.ttjobs.backend.dto.CompanyRequest;
import com.ttjobs.backend.exception.ResourceNotFoundException;
import com.ttjobs.backend.dto.CompanyMemberDTO;
import com.ttjobs.backend.dto.CompanyMemberUpsertRequest;
import com.ttjobs.backend.dto.CompanyPublicPageDTO;
import com.ttjobs.backend.dto.JobDTO;
import com.ttjobs.backend.service.CompanyService;
import java.util.List;

@RestController
@RequestMapping("/api/companies")
@Validated
public class CompanyController {

    @Autowired
    private CompanyService companyService;

    @GetMapping
    public List<CompanyDTO> getAllCompanies() {
        return companyService.getAllCompanies();
    }

    @GetMapping("/top-saved-jobs")
    public List<CompanyDTO> getTopCompaniesBySavedJobs(
            @RequestParam(defaultValue = "6") @Min(1) @Max(100) int limit) {
        return companyService.getTopCompaniesBySavedJobs(limit);
    }

    @GetMapping("/{id}")
    public CompanyDTO getCompanyById(@PathVariable Long id) {
        return companyService.getCompanyById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found"));
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
    public CompanyDTO createCompany(@Valid @RequestBody CompanyRequest request) {
        return companyService.createCompany(request);
    }

    @PutMapping("/{id}")
    public CompanyDTO updateCompany(@PathVariable Long id, @Valid @RequestBody CompanyRequest request) {
        return companyService.updateCompany(id, request);
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
}
