package com.ttjobs.backend.repository;

import com.ttjobs.backend.entity.Message;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MessageRepository extends JpaRepository<Message, Long> {
    @EntityGraph(attributePaths = {"attachments", "sender"})
    Page<Message> findByConversationIdOrderByCreatedAtDesc(Long conversationId, Pageable pageable);

    @EntityGraph(attributePaths = {"attachments", "sender"})
    Optional<Message> findTopByConversationIdOrderByCreatedAtDesc(Long conversationId);

    long countByConversationIdAndSenderIdNotAndCreatedAtAfter(Long conversationId, Long senderId, LocalDateTime createdAt);

    long countByConversationIdAndSenderIdAndCreatedAtAfter(Long conversationId, Long senderId, LocalDateTime createdAt);
}
