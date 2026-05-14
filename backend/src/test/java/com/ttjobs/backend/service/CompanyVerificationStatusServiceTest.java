package com.ttjobs.backend.service;

import com.ttjobs.backend.entity.Company;
import com.ttjobs.backend.entity.CompanyVerification;
import com.ttjobs.backend.repository.CompanyRepository;
import com.ttjobs.backend.repository.CompanyVerificationRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CompanyVerificationStatusServiceTest {

    @Mock
    private CompanyVerificationRepository companyVerificationRepository;
    @Mock
    private CompanyRepository companyRepository;

    @InjectMocks
    private CompanyVerificationStatusService companyVerificationStatusService;

    @Test
    void getEffectiveStatus_shouldPreferReviewedCompanyStatusOverStalePendingVerification() {
        Company company = new Company();
        company.setId(222L);
        company.setVerificationStatus(Company.VerificationStatus.VERIFIED);

        CompanyVerification verification = new CompanyVerification();
        verification.setCompany(company);
        verification.setStatus(Company.VerificationStatus.PENDING);

        when(companyVerificationRepository.findByCompanyId(222L)).thenReturn(Optional.of(verification));

        Company.VerificationStatus status = companyVerificationStatusService.getEffectiveStatus(company);

        assertEquals(Company.VerificationStatus.VERIFIED, status);
        assertEquals(Company.VerificationStatus.VERIFIED, verification.getStatus());
        verify(companyVerificationRepository).save(any(CompanyVerification.class));
    }
}
