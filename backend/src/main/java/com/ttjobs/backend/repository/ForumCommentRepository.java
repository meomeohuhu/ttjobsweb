package com.ttjobs.backend.repository;

import com.ttjobs.backend.entity.ForumComment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ForumCommentRepository extends JpaRepository<ForumComment, Long> {
    List<ForumComment> findByPostIdAndDeletedAtIsNullAndHiddenFalseOrderByCreatedAtAsc(Long postId);

    Optional<ForumComment> findByIdAndDeletedAtIsNull(Long id);
}
