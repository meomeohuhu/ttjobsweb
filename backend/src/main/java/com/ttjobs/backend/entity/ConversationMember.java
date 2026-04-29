package com.ttjobs.backend.entity;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Data;

@Entity
@Table(name = "conversation_members")
@Data
public class ConversationMember {

    @EmbeddedId
    private ConversationMemberId id = new ConversationMemberId();

    @MapsId("conversationId")
    @ManyToOne
    @JoinColumn(name = "conversation_id")
    private Conversation conversation;

    @MapsId("userId")
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    private LocalDateTime createdAt;
    private LocalDateTime lastReadAt;
}
