package com.ttjobs.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.ttjobs.backend.entity.JobApplication;
import java.util.List;
import java.util.Optional;

public interface JobApplicationRepository extends JpaRepository<JobApplication, Long> {
    List<JobApplication> findByUserId(Long userId);
    List<JobApplication> findByJobId(Long jobId);
    List<JobApplication> findByJobIdIn(List<Long> jobIds);
    Optional<JobApplication> findByUserIdAndJobId(Long userId, Long jobId);

    // Eager fetch to ensure job/company/user are available for CV streaming checks.
    @Query("SELECT a FROM JobApplication a " +
           "JOIN FETCH a.user " +
           "JOIN FETCH a.job j " +
           "JOIN FETCH j.company " +
           "LEFT JOIN FETCH a.cv " +
           "WHERE a.id = :id")
    Optional<JobApplication> findByIdWithDetails(@Param("id") Long id);
}
