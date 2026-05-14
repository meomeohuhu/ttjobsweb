package com.ttjobs.backend.service;

import com.ttjobs.backend.entity.Company;
import com.ttjobs.backend.entity.CompanyVerification;
import com.ttjobs.backend.repository.CompanyRepository;
import com.ttjobs.backend.repository.CompanyVerificationRepository;
import java.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CompanyVerificationStatusService {

    @Autowired
    private CompanyVerificationRepository companyVerificationRepository;
    @Autowired
    private CompanyRepository companyRepository;

    @Transactional
    public Company.VerificationStatus getEffectiveStatus(Company company) {
        if (company == null) {
            return Company.VerificationStatus.PENDING;
        }
        Company.VerificationStatus companyStatus = company.getVerificationStatus();
        CompanyVerification verification = company.getId() == null
                ? null
                : companyVerificationRepository.findByCompanyId(company.getId()).orElse(null);
        Company.VerificationStatus verificationStatus = verification == null ? null : verification.getStatus();

        Company.VerificationStatus effectiveStatus = resolveStatus(companyStatus, verificationStatus, company, verification);
        if (effectiveStatus == null) {
            effectiveStatus = Company.VerificationStatus.PENDING;
        }
        if (companyStatus != effectiveStatus) {
            company.setVerificationStatus(effectiveStatus);
            companyRepository.save(company);
        }
        if (verification != null && verificationStatus != effectiveStatus) {
            verification.setStatus(effectiveStatus);
            companyVerificationRepository.save(verification);
        }
        return effectiveStatus;
    }

    @Transactional
    public boolean isVerified(Company company) {
        return getEffectiveStatus(company) == Company.VerificationStatus.VERIFIED;
    }

    private Company.VerificationStatus resolveStatus(
            Company.VerificationStatus companyStatus,
            Company.VerificationStatus verificationStatus,
            Company company,
            CompanyVerification verification
    ) {
        Company.VerificationStatus normalizedCompanyStatus = companyStatus == null
                ? Company.VerificationStatus.PENDING
                : companyStatus;
        if (verificationStatus == null) {
            return normalizedCompanyStatus;
        }
        if (normalizedCompanyStatus == verificationStatus) {
            return normalizedCompanyStatus;
        }
        if (normalizedCompanyStatus == Company.VerificationStatus.PENDING) {
            return verificationStatus;
        }
        if (verificationStatus == Company.VerificationStatus.PENDING) {
            return normalizedCompanyStatus;
        }

        LocalDateTime companyUpdatedAt = company == null ? null : company.getUpdatedAt();
        LocalDateTime verificationUpdatedAt = verification == null
                ? null
                : verification.getReviewedAt() != null ? verification.getReviewedAt() : verification.getUpdatedAt();
        if (companyUpdatedAt != null && verificationUpdatedAt != null) {
            return companyUpdatedAt.isAfter(verificationUpdatedAt) ? normalizedCompanyStatus : verificationStatus;
        }
        return normalizedCompanyStatus;
    }
}
