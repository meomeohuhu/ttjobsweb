package com.ttjobs.backend.dto.skill;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateSkillRequest {
    @NotBlank
    @Size(max = 120)
    private String name;
}

