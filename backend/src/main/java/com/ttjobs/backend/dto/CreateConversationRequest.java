package com.ttjobs.backend.dto;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import lombok.Data;

@Data
public class CreateConversationRequest {
    @NotEmpty(message = "memberIds is required")
    private List<Long> memberIds;
}
