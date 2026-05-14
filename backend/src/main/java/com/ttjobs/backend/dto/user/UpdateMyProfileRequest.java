package com.ttjobs.backend.dto.user;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class UpdateMyProfileRequest {

    @Size(max = 120, message = "name must be at most 120 characters")
    private String name;

    @Size(max = 30, message = "phone must be at most 30 characters")
    private String phone;

    @Size(max = 255, message = "address must be at most 255 characters")
    private String address;

    @Min(value = 0, message = "experienceYears must be >= 0")
    private Integer experienceYears;

    private String cvRole;
    private String cvObjective;
    private String cvExperienceHighlights;

    private List<@Size(max = 64, message = "skill name must be at most 64 characters") String> skills;
}

