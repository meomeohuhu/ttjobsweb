package com.ttjobs.backend.repository;

import com.ttjobs.backend.entity.AiMatchEvent;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AiMatchEventRepository extends JpaRepository<AiMatchEvent, Long> {

    List<AiMatchEvent> findAllByOrderByCreatedAtDesc(Pageable pageable);

    @Query("""
            select e from AiMatchEvent e
            where (:eventType is null or e.eventType = :eventType)
              and (:label is null or e.predictedLabel = :label)
              and (:from is null or e.createdAt >= :from)
              and (:to is null or e.createdAt <= :to)
              and (:minScore is null or e.predictedScore >= :minScore)
              and (:maxScore is null or e.predictedScore <= :maxScore)
            order by e.createdAt desc
            """)
    List<AiMatchEvent> searchTrainingEvents(
            @Param("eventType") String eventType,
            @Param("label") String label,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to,
            @Param("minScore") Integer minScore,
            @Param("maxScore") Integer maxScore,
            Pageable pageable);

    long countByEventType(String eventType);

    @Query("select e.eventType, count(e) from AiMatchEvent e group by e.eventType")
    List<Object[]> countByEventTypeGroup();

    @Query("select e.predictedLabel, count(e) from AiMatchEvent e where e.predictedLabel is not null group by e.predictedLabel")
    List<Object[]> countByPredictedLabelGroup();

    @Query(value = """
            select coalesce(j.category, 'UNKNOWN') as category, count(*) as total
            from ai_match_events e
            left join jobs j on j.id = e.job_id
            where e.predicted_label = 'match'
            group by coalesce(j.category, 'UNKNOWN')
            order by total desc
            limit 8
            """, nativeQuery = true)
    List<Object[]> topMatchedIndustries();
}
