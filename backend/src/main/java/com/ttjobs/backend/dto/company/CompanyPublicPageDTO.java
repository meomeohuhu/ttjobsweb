package com.ttjobs.backend.dto.company;

import com.ttjobs.backend.dto.job.JobDTO;

import com.ttjobs.backend.dto.company.CompanyDTO;

import lombok.Data;

import java.util.List;

@Data
public class CompanyPublicPageDTO {
    private CompanyDTO company;
    private List<JobDTO> jobs;
}

