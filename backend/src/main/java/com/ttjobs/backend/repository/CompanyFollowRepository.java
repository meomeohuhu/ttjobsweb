package com.ttjobs.backend.repository;

import com.ttjobs.backend.entity.CompanyFollow;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CompanyFollowRepository extends JpaRepository<CompanyFollow, Long> {
    boolean existsByUserIdAndCompanyId(Long userId, Long companyId);
    Optional<CompanyFollow> findByUserIdAndCompanyId(Long userId, Long companyId);
    long countByCompanyId(Long companyId);
    List<CompanyFollow> findByUserIdOrderByFollowedAtDesc(Long userId);
    List<CompanyFollow> findByCompanyId(Long companyId);
}
