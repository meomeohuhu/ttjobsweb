package com.ttjobs.backend.repository;

import com.ttjobs.backend.entity.Company;
import com.ttjobs.backend.entity.CompanyVerification;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompanyVerificationRepository extends JpaRepository<CompanyVerification, Long> {
    Optional<CompanyVerification> findByCompanyId(Long companyId);
    List<CompanyVerification> findByStatusOrderByCreatedAtDesc(Company.VerificationStatus status);
    List<CompanyVerification> findAllByOrderByCreatedAtDesc();
}
