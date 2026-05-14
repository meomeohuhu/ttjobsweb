package com.ttjobs.backend.repository;

import com.ttjobs.backend.entity.ForumPost;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ForumPostRepository extends JpaRepository<ForumPost, Long> {
    Page<ForumPost> findByDeletedAtIsNullAndHiddenFalseOrderByCreatedAtDesc(Pageable pageable);

    Page<ForumPost> findByTagAndDeletedAtIsNullAndHiddenFalseOrderByCreatedAtDesc(String tag, Pageable pageable);

    Optional<ForumPost> findByIdAndDeletedAtIsNull(Long id);
}
