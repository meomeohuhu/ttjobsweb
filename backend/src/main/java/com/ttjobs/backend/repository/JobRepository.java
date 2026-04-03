package com.ttjobs.backend.repository;

import com.ttjobs.backend.entity.CompanyMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.ttjobs.backend.entity.Job;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface JobRepository extends JpaRepository<Job, Long> {

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
    @Query("SELECT j FROM Job j WHERE j.deletedAt IS NULL AND j.company.deletedAt IS NULL AND " +
           "(j.company.createdBy.id = :userId OR EXISTS (" +
           "SELECT 1 FROM CompanyMember cm WHERE cm.company.id = j.company.id AND cm.user.id = :userId " +
           "AND cm.memberRole IN :roles))")
    List<Job> findManagedJobsByRecruiterId(@Param("userId") Long userId,
                                           @Param("roles") Collection<CompanyMember.MemberRole> roles);
}
