package com.ttjobs.backend.repository;

import com.ttjobs.backend.entity.InterviewSchedule;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InterviewScheduleRepository extends JpaRepository<InterviewSchedule, Long> {
    List<InterviewSchedule> findByApplicationJobIdInOrderByScheduledAtAsc(List<Long> jobIds);
    List<InterviewSchedule> findByCandidateIdOrderByScheduledAtAsc(Long candidateId);
    long countByApplicationJobIdInAndScheduledAtBetween(List<Long> jobIds, LocalDateTime from, LocalDateTime to);
}
