package com.ttjobs.backend.dto.conversation;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class ReadReceiptDTO {
    private Long conversationId;
    private Long userId;
    private LocalDateTime lastReadAt;
}
