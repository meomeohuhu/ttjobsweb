package com.ttjobs.backend.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class CompanyReviewSummaryDTO {
    private Long companyId;
    private Double averageRating;
    private BigDecimal averageSalary;
    private Long reviewCount;
    private List<CompanyReviewDTO> reviews;
}

