package com.ttjobs.backend.repository;

import com.ttjobs.backend.entity.JobApplicationStatusAudit;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JobApplicationStatusAuditRepository extends JpaRepository<JobApplicationStatusAudit, Long> {
    List<JobApplicationStatusAudit> findByApplicationIdOrderByChangedAtAsc(Long applicationId);
}
