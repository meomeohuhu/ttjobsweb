package com.ttjobs.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.ttjobs.backend.entity.Company;

public interface CompanyRepository extends JpaRepository<Company, Long> {
}