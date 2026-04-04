package com.ttjobs.backend.repository;

import com.ttjobs.backend.entity.SavedJob;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SavedJobRepository extends JpaRepository<SavedJob, Long> {
    boolean existsByUserIdAndJobId(Long userId, Long jobId);
    Optional<SavedJob> findByUserIdAndJobId(Long userId, Long jobId);
    List<SavedJob> findByUserIdOrderBySavedAtDesc(Long userId);
}
