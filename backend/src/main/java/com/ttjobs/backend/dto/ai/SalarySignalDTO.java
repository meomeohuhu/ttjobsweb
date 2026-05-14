package com.ttjobs.backend.dto.ai;

import lombok.Data;

@Data
public class SalarySignalDTO {
    private Long min;
    private Long max;
    private String currency;
}
