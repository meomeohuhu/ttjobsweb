package com.ttjobs.backend.dto.company;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CompanyMemberUpsertRequest {
    @NotNull(message = "userId is required")
    private Long userId;

    @NotBlank(message = "memberRole is required")
    private String memberRole;
}

