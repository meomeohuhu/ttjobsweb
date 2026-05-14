package com.ttjobs.backend.dto.company;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CompanyReviewDTO {
    private Long id;
    private Long companyId;
    private String companyName;
    private Integer rating;
    private String pros;
    private String cons;
    private BigDecimal salary;
    private Boolean anonymous;
    private String reviewerName;
    private LocalDateTime createdAt;
}


