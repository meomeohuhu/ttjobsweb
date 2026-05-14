package com.ttjobs.backend.dto.conversation;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class ConversationRealtimeEventDTO {
    private String type;
    private Long conversationId;
    private Long actorId;
    private Object payload;
    private LocalDateTime occurredAt;
}
