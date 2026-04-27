package com.ttjobs.backend.repository;

import com.ttjobs.backend.entity.RecruitmentEvent;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecruitmentEventRepository extends JpaRepository<RecruitmentEvent, Long> {
    long countByJobIdInAndEventTypeAndCreatedAtBetween(List<Long> jobIds, String eventType, LocalDateTime from, LocalDateTime to);
}
