package com.ttjobs.backend.repository;

import com.ttjobs.backend.entity.CompanyReview;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CompanyReviewRepository extends JpaRepository<CompanyReview, Long> {
    List<CompanyReview> findByCompanyIdOrderByCreatedAtDesc(Long companyId);
}

