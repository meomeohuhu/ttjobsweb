package com.ttjobs.backend.repository;

import com.ttjobs.backend.entity.ApplicationAiScore;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ApplicationAiScoreRepository extends JpaRepository<ApplicationAiScore, Long> {

    Optional<ApplicationAiScore> findByApplicationId(Long applicationId);
}
