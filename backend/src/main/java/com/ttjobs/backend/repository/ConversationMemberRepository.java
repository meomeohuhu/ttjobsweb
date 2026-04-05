package com.ttjobs.backend.repository;

import com.ttjobs.backend.entity.ConversationMember;
import com.ttjobs.backend.entity.ConversationMemberId;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConversationMemberRepository extends JpaRepository<ConversationMember, ConversationMemberId> {
    boolean existsByIdConversationIdAndIdUserId(Long conversationId, Long userId);
    List<ConversationMember> findByIdUserId(Long userId);
    List<ConversationMember> findByIdConversationId(Long conversationId);
    List<ConversationMember> findByIdConversationIdIn(List<Long> conversationIds);
}
