package com.ttjobs.backend.dto.tool;

import lombok.Data;

@Data
public class SalaryBenchmarkDTO {
    private String industry;
    private String location;
    private String level;
    private Integer p25;
    private Integer p50;
    private Integer p75;
}

