package com.ttjobs.backend.repository;

import com.ttjobs.backend.entity.Company;
import com.ttjobs.backend.entity.CompanyMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import com.ttjobs.backend.entity.Job;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface JobRepository extends JpaRepository<Job, Long>, JpaSpecificationExecutor<Job> {

    @Query("SELECT j FROM Job j WHERE j.deletedAt IS NULL AND j.company.deletedAt IS NULL AND " +
           "(:title IS NULL OR LOWER(j.title) LIKE LOWER(CONCAT('%', :title, '%'))) AND " +
           "(:location IS NULL OR LOWER(j.location) LIKE LOWER(CONCAT('%', :location, '%'))) AND " +
           "(:companyName IS NULL OR LOWER(j.company.name) LIKE LOWER(CONCAT('%', :companyName, '%'))) AND " +
           "(:jobType IS NULL OR j.jobType = :jobType) AND " +
           "(:experienceLevel IS NULL OR j.experienceLevel = :experienceLevel) AND " +
           "(:status IS NULL OR j.status = :status)")
    Page<Job> findJobsWithFilters(@Param("title") String title,
                                  @Param("location") String location,
                                  @Param("companyName") String companyName,
                                  @Param("jobType") String jobType,
                                  @Param("experienceLevel") String experienceLevel,
                                  @Param("status") String status,
                                  Pageable pageable);

    Optional<Job> findByIdAndDeletedAtIsNull(Long id);
    List<Job> findByCompanyIdAndDeletedAtIsNull(Long companyId);
    List<Job> findByCompanyCreatedById(Long createdById);

    @Query("SELECT COUNT(j.id) FROM Job j " +
           "WHERE j.deletedAt IS NULL AND j.company.deletedAt IS NULL AND " +
           "j.company.id = :companyId AND LOWER(j.status) = LOWER(:status)")
    long countByCompanyIdAndStatus(@Param("companyId") Long companyId, @Param("status") String status);

    @Query("SELECT COUNT(sj.id) FROM SavedJob sj " +
           "WHERE sj.job.deletedAt IS NULL AND sj.job.company.deletedAt IS NULL AND " +
           "sj.job.company.id = :companyId")
    long countSavedJobsByCompanyId(@Param("companyId") Long companyId);

    @Query("SELECT j as job, COUNT(sj.id) as savedCount " +
           "FROM Job j LEFT JOIN SavedJob sj ON sj.job = j " +
           "WHERE j.deletedAt IS NULL AND j.company.deletedAt IS NULL AND " +
           "j.company.verificationStatus = :verificationStatus AND " +
           "j.company.id = :companyId AND LOWER(j.status) = LOWER(:status) " +
           "AND (j.applicationDeadline IS NULL OR j.applicationDeadline >= CURRENT_TIMESTAMP) " +
           "GROUP BY j ORDER BY j.postedDate DESC")
    List<JobWithSavedCount> findCompanyJobsWithSavedCount(@Param("companyId") Long companyId,
                                                          @Param("status") String status,
                                                          @Param("verificationStatus") Company.VerificationStatus verificationStatus,
                                                          Pageable pageable);

    @Query("SELECT j FROM Job j WHERE j.deletedAt IS NULL AND j.company.deletedAt IS NULL AND " +
           "(j.company.createdBy.id = :userId OR EXISTS (" +
           "SELECT 1 FROM CompanyMember cm WHERE cm.company.id = j.company.id AND cm.user.id = :userId " +
           "AND cm.memberRole IN :roles))")
    List<Job> findManagedJobsByRecruiterId(@Param("userId") Long userId,
                                           @Param("roles") Collection<CompanyMember.MemberRole> roles);

    @Query("SELECT j as job, COUNT(sj.id) as savedCount " +
           "FROM Job j LEFT JOIN SavedJob sj ON sj.job = j " +
           "WHERE j.deletedAt IS NULL AND j.company.deletedAt IS NULL AND j.status = :status " +
           "AND j.company.verificationStatus = :verificationStatus " +
           "AND (j.applicationDeadline IS NULL OR j.applicationDeadline >= CURRENT_TIMESTAMP) " +
           "GROUP BY j")
    List<JobWithSavedCount> findJobsWithSavedCount(@Param("status") String status,
                                                   @Param("verificationStatus") Company.VerificationStatus verificationStatus,
                                                   Pageable pageable);

    @Query("SELECT j as job, COUNT(sj.id) as savedCount " +
           "FROM Job j LEFT JOIN SavedJob sj ON sj.job = j " +
           "WHERE j.deletedAt IS NULL AND j.company.deletedAt IS NULL AND LOWER(j.status) = LOWER(:status) " +
           "AND j.company.verificationStatus = :verificationStatus " +
           "AND (j.applicationDeadline IS NULL OR j.applicationDeadline >= CURRENT_TIMESTAMP) " +
           "GROUP BY j " +
           "ORDER BY COALESCE(j.salaryMax, j.salary, j.salaryMin, 0) DESC, COUNT(sj.id) DESC, j.postedDate DESC")
    List<JobWithSavedCount> findHighlightedJobs(@Param("status") String status,
                                                @Param("verificationStatus") Company.VerificationStatus verificationStatus,
                                                Pageable pageable);

    @Query("SELECT j as job, COUNT(sj.id) as savedCount " +
           "FROM Job j LEFT JOIN SavedJob sj ON sj.job = j " +
           "WHERE j.deletedAt IS NULL AND j.company.deletedAt IS NULL AND LOWER(j.status) = LOWER(:status) " +
           "AND j.company.verificationStatus = :verificationStatus " +
           "AND (j.applicationDeadline IS NULL OR j.applicationDeadline >= CURRENT_TIMESTAMP) " +
           "GROUP BY j " +
           "ORDER BY COUNT(sj.id) DESC, j.postedDate DESC")
    List<JobWithSavedCount> findBestJobs(@Param("status") String status,
                                         @Param("verificationStatus") Company.VerificationStatus verificationStatus,
                                         Pageable pageable);

    @Query("SELECT j.category as category, COUNT(j.id) as jobCount " +
           "FROM Job j " +
           "WHERE j.deletedAt IS NULL AND j.company.deletedAt IS NULL AND LOWER(j.status) = LOWER(:status) " +
           "AND j.company.verificationStatus = :verificationStatus " +
           "AND (j.applicationDeadline IS NULL OR j.applicationDeadline >= CURRENT_TIMESTAMP) " +
           "GROUP BY j.category " +
           "ORDER BY COUNT(j.id) DESC")
    List<JobCategoryCount> findTopCategories(@Param("status") String status,
                                             @Param("verificationStatus") Company.VerificationStatus verificationStatus,
                                             Pageable pageable);
}
