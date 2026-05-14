package com.ttjobs.backend.dto.conversation;

import com.ttjobs.backend.dto.conversation.MessageAttachmentDTO;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;

@Data
public class MessageDTO {
    private Long id;
    private Long conversationId;
    private Long senderId;
    private String content;
    private String type;
    private LocalDateTime createdAt;
    private List<MessageAttachmentDTO> attachments;
}

