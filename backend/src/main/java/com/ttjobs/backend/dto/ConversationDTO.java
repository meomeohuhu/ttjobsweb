package com.ttjobs.backend.dto;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;

@Data
public class ConversationDTO {
    private Long id;
    private LocalDateTime createdAt;
    private List<Long> memberIds;
}
