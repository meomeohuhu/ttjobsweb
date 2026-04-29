package com.ttjobs.backend.repository;

import com.ttjobs.backend.entity.RecruitmentCampaign;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecruitmentCampaignRepository extends JpaRepository<RecruitmentCampaign, Long> {
    @EntityGraph(attributePaths = {"company", "jobs", "applications"})
    List<RecruitmentCampaign> findByCompanyIdInOrderByCreatedAtDesc(List<Long> companyIds);
}
