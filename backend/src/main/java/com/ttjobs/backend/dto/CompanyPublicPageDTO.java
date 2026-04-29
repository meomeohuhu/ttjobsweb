package com.ttjobs.backend.dto;

import lombok.Data;

import java.util.List;

@Data
public class CompanyPublicPageDTO {
    private CompanyDTO company;
    private List<JobDTO> jobs;
}
