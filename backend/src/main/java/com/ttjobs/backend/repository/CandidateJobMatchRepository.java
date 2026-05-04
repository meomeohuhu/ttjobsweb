package com.ttjobs.backend.repository;

import com.ttjobs.backend.entity.CandidateJobMatch;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface CandidateJobMatchRepository extends JpaRepository<CandidateJobMatch, Long> {

    List<CandidateJobMatch> findByUserIdAndPreferenceUpdatedAtOrderByScoreDescCreatedAtDesc(
            Long userId,
            LocalDateTime preferenceUpdatedAt,
            Pageable pageable
    );

    void deleteByUserId(Long userId);
}
