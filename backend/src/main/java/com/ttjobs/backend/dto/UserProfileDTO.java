package com.ttjobs.backend.dto;

import lombok.Data;

import java.util.List;

@Data
public class UserProfileDTO {
    private Long id;
    private String name;
    private String email;
    private String role;
    private String phone;
    private String address;
    private Integer experienceYears;
    private String cvUrl;
    private String cvRole;
    private String cvObjective;
    private String cvExperienceHighlights;
    private String avatarUrl;
    private List<String> skills;
}
