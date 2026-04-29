package com.ttjobs.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.ttjobs.backend.entity.Company;

import java.util.List;
import java.util.Optional;

public interface CompanyRepository extends JpaRepository<Company, Long> {
    List<Company> findByDeletedAtIsNull();
    Optional<Company> findByIdAndDeletedAtIsNull(Long id);
}
