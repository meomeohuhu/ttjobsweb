package com.ttjobs.backend.service;

import com.ttjobs.backend.dto.common.AdminActionRequest;
import com.ttjobs.backend.dto.company.CompanyVerificationDTO;
import com.ttjobs.backend.dto.company.CompanyVerificationRequest;
import com.ttjobs.backend.entity.Company;
import com.ttjobs.backend.entity.CompanyVerification;
import com.ttjobs.backend.entity.User;
import com.ttjobs.backend.repository.CompanyRepository;
import com.ttjobs.backend.repository.CompanyVerificationRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class CompanyVerificationService {

    @Autowired
    private CompanyRepository companyRepository;
    @Autowired
    private CompanyVerificationRepository companyVerificationRepository;
    @Autowired
    private AuthContextService authContextService;
    @Autowired
    private CompanyAuthorizationService companyAuthorizationService;
    @Autowired
    private AdminAuditLogService adminAuditLogService;
    @Autowired
    private CompanyVerificationStatusService companyVerificationStatusService;

    @Transactional(readOnly = true)
    public CompanyVerificationDTO getMyVerification(Long companyId) {
        User currentUser = authContextService.requireCurrentUser();
        Company company = requireCompany(companyId);
        requireManage(currentUser, company);
        return companyVerificationRepository.findByCompanyId(companyId)
                .map(this::toDto)
                .orElseGet(() -> emptyDto(company));
    }

    @Transactional
    public CompanyVerificationDTO upsertVerification(Long companyId, CompanyVerificationRequest request) {
        User currentUser = authContextService.requireCurrentUser();
        Company company = requireCompany(companyId);
        requireManage(currentUser, company);

        CompanyVerification verification = companyVerificationRepository.findByCompanyId(companyId).orElseGet(() -> {
            CompanyVerification created = new CompanyVerification();
            created.setCompany(company);
            return created;
        });
        verification.setBusinessLicenseUrl(trim(request.getBusinessLicenseUrl()));
        verification.setTaxCode(trim(request.getTaxCode()));
        verification.setWebsite(trim(request.getWebsite()));
        verification.setNote(trim(request.getNote()));
        verification.setStatus(Company.VerificationStatus.PENDING);
        verification.setReviewReason(null);
        verification.setReviewedBy(null);
        verification.setReviewedAt(null);
        company.setVerificationStatus(Company.VerificationStatus.PENDING);
        companyRepository.save(company);
        return toDto(companyVerificationRepository.save(verification));
    }

    @Transactional
    public List<CompanyVerificationDTO> getAdminQueue(String status) {
        Company.VerificationStatus parsed = parseStatus(status, null);
        List<CompanyVerification> values = companyVerificationRepository.findAllByOrderByCreatedAtDesc();
        List<CompanyVerificationDTO> result = new ArrayList<>(values.stream()
                .filter(verification -> matchesStatus(verification, parsed))
                .map(this::toDto)
                .toList());
        Set<Long> companyIdsWithVerification = result.stream()
                .map(CompanyVerificationDTO::getCompanyId)
                .collect(Collectors.toSet());

        List<Company> companiesWithoutVerification = parsed == null
                ? companyRepository.findByDeletedAtIsNull()
                : companyRepository.findByDeletedAtIsNullAndVerificationStatus(parsed);
        companiesWithoutVerification.stream()
                .filter(company -> company.getId() != null && !companyIdsWithVerification.contains(company.getId()))
                .map(this::emptyDto)
                .forEach(result::add);
        return result;
    }

    @Transactional
    public CompanyVerificationDTO review(Long companyId, Company.VerificationStatus status, AdminActionRequest request) {
        if (status != Company.VerificationStatus.VERIFIED
                && status != Company.VerificationStatus.REJECTED
                && status != Company.VerificationStatus.SUSPENDED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid verification action");
        }
        String reason = requireReason(request == null ? null : request.getReason());
        User admin = authContextService.requireCurrentUser();
        Company company = requireCompany(companyId);
        CompanyVerification verification = companyVerificationRepository.findByCompanyId(companyId).orElseGet(() -> {
            CompanyVerification created = new CompanyVerification();
            created.setCompany(company);
            return created;
        });
        company.setVerificationStatus(status);
        verification.setStatus(status);
        verification.setReviewReason(reason);
        verification.setReviewedBy(admin);
        verification.setReviewedAt(LocalDateTime.now());
        companyRepository.save(company);
        CompanyVerification saved = companyVerificationRepository.save(verification);
        adminAuditLogService.log("COMPANY_" + status.name(), "COMPANY", companyId, reason, company.getName());
        return toDto(saved);
    }

    private Company requireCompany(Long companyId) {
        return companyRepository.findByIdAndDeletedAtIsNull(companyId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Company not found"));
    }

    private void requireManage(User user, Company company) {
        if (authContextService.isAdmin(user)) {
            return;
        }
        companyAuthorizationService.requireManageCompany(user, company);
    }

    private Company.VerificationStatus parseStatus(String value, Company.VerificationStatus fallback) {
        if (value == null || value.isBlank()) return fallback;
        try {
            String normalized = value.trim().toUpperCase();
            if ("VERIFY".equals(normalized) || "APPROVED".equals(normalized)) {
                return Company.VerificationStatus.VERIFIED;
            }
            if ("REJECT".equals(normalized)) {
                return Company.VerificationStatus.REJECTED;
            }
            if ("SUSPEND".equals(normalized)) {
                return Company.VerificationStatus.SUSPENDED;
            }
            return Company.VerificationStatus.valueOf(normalized);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid verification status");
        }
    }

    private String requireReason(String reason) {
        String clean = trim(reason);
        if (clean == null || clean.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "reason is required");
        }
        return clean;
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }

    private boolean matchesStatus(CompanyVerification verification, Company.VerificationStatus expected) {
        Company.VerificationStatus effectiveStatus = resolveEffectiveStatus(verification);
        return expected == null || effectiveStatus == expected;
    }

    private Company.VerificationStatus resolveEffectiveStatus(CompanyVerification verification) {
        return companyVerificationStatusService.getEffectiveStatus(verification.getCompany());
    }

    private CompanyVerificationDTO emptyDto(Company company) {
        CompanyVerificationDTO dto = new CompanyVerificationDTO();
        dto.setCompanyId(company.getId());
        dto.setCompanyName(company.getName());
        dto.setStatus(company.getVerificationStatus() == null ? "PENDING" : company.getVerificationStatus().name());
        return dto;
    }

    private CompanyVerificationDTO toDto(CompanyVerification verification) {
        CompanyVerificationDTO dto = new CompanyVerificationDTO();
        dto.setId(verification.getId());
        dto.setCompanyId(verification.getCompany() == null ? null : verification.getCompany().getId());
        dto.setCompanyName(verification.getCompany() == null ? null : verification.getCompany().getName());
        dto.setBusinessLicenseUrl(verification.getBusinessLicenseUrl());
        dto.setTaxCode(verification.getTaxCode());
        dto.setWebsite(verification.getWebsite());
        dto.setNote(verification.getNote());
        Company.VerificationStatus effectiveStatus = resolveEffectiveStatus(verification);
        dto.setStatus(effectiveStatus.name());
        dto.setReviewReason(verification.getReviewReason());
        dto.setReviewedById(verification.getReviewedBy() == null ? null : verification.getReviewedBy().getId());
        dto.setReviewedByName(verification.getReviewedBy() == null ? null : verification.getReviewedBy().getName());
        dto.setReviewedAt(verification.getReviewedAt());
        dto.setCreatedAt(verification.getCreatedAt());
        dto.setUpdatedAt(verification.getUpdatedAt());
        return dto;
    }
}
