package com.ttjobs.backend.repository;

import com.ttjobs.backend.entity.Conversation;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConversationRepository extends JpaRepository<Conversation, Long> {
    List<Conversation> findByIdInOrderByCreatedAtDesc(List<Long> ids);
}
