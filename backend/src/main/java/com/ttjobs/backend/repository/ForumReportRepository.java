package com.ttjobs.backend.repository;

import com.ttjobs.backend.entity.ForumReport;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ForumReportRepository extends JpaRepository<ForumReport, Long> {
    Page<ForumReport> findByStatusOrderByCreatedAtDesc(ForumReport.Status status, Pageable pageable);
}
