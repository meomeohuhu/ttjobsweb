package com.ttjobs.backend.repository;

import com.ttjobs.backend.entity.RecruitmentCampaign;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RecruitmentCampaignRepository extends JpaRepository<RecruitmentCampaign, Long> {
    @EntityGraph(attributePaths = {"company", "jobs"})
    List<RecruitmentCampaign> findByCompanyIdInOrderByCreatedAtDesc(List<Long> companyIds);

    @Query("SELECT COUNT(a.id) FROM RecruitmentCampaign c JOIN c.applications a WHERE c.id = :campaignId")
    long countApplicationsByCampaignId(@Param("campaignId") Long campaignId);
}
