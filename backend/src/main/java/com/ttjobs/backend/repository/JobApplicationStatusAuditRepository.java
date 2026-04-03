package com.ttjobs.backend.repository;

import com.ttjobs.backend.entity.JobApplicationStatusAudit;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JobApplicationStatusAuditRepository extends JpaRepository<JobApplicationStatusAudit, Long> {
}
