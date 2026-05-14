package com.ttjobs.backend.repository;

import com.ttjobs.backend.entity.ForumLike;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ForumLikeRepository extends JpaRepository<ForumLike, Long> {
    boolean existsByPostIdAndUserId(Long postId, Long userId);

    Optional<ForumLike> findByPostIdAndUserId(Long postId, Long userId);

    long countByPostId(Long postId);
}
