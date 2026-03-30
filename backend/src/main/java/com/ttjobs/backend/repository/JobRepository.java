package com.ttjobs.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.ttjobs.backend.entity.Job;
import java.util.List;

public interface JobRepository extends JpaRepository<Job, Long> {

    @Query("SELECT j FROM Job j WHERE " +
           "(:title IS NULL OR LOWER(j.title) LIKE LOWER(CONCAT('%', :title, '%'))) AND " +
           "(:location IS NULL OR LOWER(j.location) LIKE LOWER(CONCAT('%', :location, '%'))) AND " +
           "(:companyName IS NULL OR LOWER(j.company.name) LIKE LOWER(CONCAT('%', :companyName, '%'))) AND " +
           "(:jobType IS NULL OR j.jobType = :jobType) AND " +
           "(:experienceLevel IS NULL OR j.experienceLevel = :experienceLevel)")
    List<Job> findJobsWithFilters(@Param("title") String title,
                                  @Param("location") String location,
                                  @Param("companyName") String companyName,
                                  @Param("jobType") String jobType,
                                  @Param("experienceLevel") String experienceLevel);
}