package com.ttjobs.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.ttjobs.backend.entity.JobApplication;
import java.util.List;
import java.util.Optional;

public interface JobApplicationRepository extends JpaRepository<JobApplication, Long> {
    List<JobApplication> findByUserId(Long userId);
    List<JobApplication> findByJobId(Long jobId);
    List<JobApplication> findByJobIdIn(List<Long> jobIds);
    Optional<JobApplication> findByUserIdAndJobId(Long userId, Long jobId);
}
