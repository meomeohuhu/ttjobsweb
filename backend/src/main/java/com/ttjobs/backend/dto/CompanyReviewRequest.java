package com.ttjobs.backend.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CompanyReviewRequest {
    private Integer rating;
    private String pros;
    private String cons;
    private BigDecimal salary;
    private Boolean anonymous;
}

