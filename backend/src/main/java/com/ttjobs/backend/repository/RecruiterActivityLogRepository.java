package com.ttjobs.backend.repository;

import com.ttjobs.backend.entity.RecruiterActivityLog;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RecruiterActivityLogRepository extends JpaRepository<RecruiterActivityLog, Long> {
    List<RecruiterActivityLog> findByActorIdOrderByCreatedAtDesc(Long actorId, Pageable pageable);
}
