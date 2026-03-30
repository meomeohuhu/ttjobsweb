package com.ttjobs.backend.dto;

import lombok.Data;

@Data
public class CompanyDTO {
    private Long id;
    private String name;
    private String description;
    private String location;
    private String website;
    private String industry;
}