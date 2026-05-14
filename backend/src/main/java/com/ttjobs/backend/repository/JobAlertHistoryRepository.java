package com.ttjobs.backend.repository;

import com.ttjobs.backend.entity.JobAlertHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JobAlertHistoryRepository extends JpaRepository<JobAlertHistory, Long> {
    boolean existsByUserIdAndJobId(Long userId, Long jobId);
    boolean existsByUserIdAndSavedSearchIdAndJobId(Long userId, Long savedSearchId, Long jobId);
}
