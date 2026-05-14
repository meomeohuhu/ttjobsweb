package com.ttjobs.backend.repository;

import com.ttjobs.backend.entity.AiServiceCallLog;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface AiServiceCallLogRepository extends JpaRepository<AiServiceCallLog, Long> {

    long countByStatus(String status);

    long countByFallbackUsedTrue();

    @Query("select avg(l.latencyMs) from AiServiceCallLog l")
    Double averageLatencyMs();

    @Query("select l.predictedLabel, count(l) from AiServiceCallLog l where l.predictedLabel is not null group by l.predictedLabel")
    List<Object[]> countByPredictedLabelGroup();
}
