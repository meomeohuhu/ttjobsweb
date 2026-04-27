package com.ttjobs.backend.repository;

import com.ttjobs.backend.entity.MessageAttachment;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MessageAttachmentRepository extends JpaRepository<MessageAttachment, Long> {
    Optional<MessageAttachment> findByIdAndMessageConversationId(Long id, Long conversationId);
}
