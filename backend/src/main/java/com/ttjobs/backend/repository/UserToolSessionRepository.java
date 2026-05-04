package com.ttjobs.backend.repository;

import com.ttjobs.backend.entity.UserToolSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserToolSessionRepository extends JpaRepository<UserToolSession, Long> {
    List<UserToolSession> findByUserIdOrderByCreatedAtDesc(Long userId);
    List<UserToolSession> findByUserIdAndToolSlugOrderByCreatedAtDesc(Long userId, String toolSlug);
}
