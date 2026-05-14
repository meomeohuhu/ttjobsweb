package com.ttjobs.backend.dto.ai;

import lombok.Data;

import java.util.List;

@Data
public class JobRequirementSignalDTO {
    private String normalizedTitle;
    private String seniority;
    private List<String> skills;
    private List<String> industries;
    private List<String> locations;
    private SalarySignalDTO salaryExpectation;
    private List<String> languages;
    private List<String> evidence;
}
