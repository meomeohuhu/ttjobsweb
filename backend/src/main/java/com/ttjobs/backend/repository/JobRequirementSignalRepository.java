package com.ttjobs.backend.repository;

import com.ttjobs.backend.entity.JobRequirementSignal;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JobRequirementSignalRepository extends JpaRepository<JobRequirementSignal, Long> {
    Optional<JobRequirementSignal> findByJobId(Long jobId);
}
