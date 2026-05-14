package com.ttjobs.backend.repository;

import com.ttjobs.backend.entity.CandidateProfileSignal;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CandidateProfileSignalRepository extends JpaRepository<CandidateProfileSignal, Long> {
    List<CandidateProfileSignal> findByUserIdOrderByUpdatedAtDesc(Long userId);
}
