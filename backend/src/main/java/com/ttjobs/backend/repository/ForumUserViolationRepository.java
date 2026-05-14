package com.ttjobs.backend.repository;

import com.ttjobs.backend.entity.ForumUserViolation;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ForumUserViolationRepository extends JpaRepository<ForumUserViolation, Long> {
    Optional<ForumUserViolation> findByUserId(Long userId);
    List<ForumUserViolation> findAllByOrderByLastActionAtDesc();
}
