package com.ttjobs.backend.dto.common;

import lombok.Data;

@Data
public class CompanySummaryDTO {
    private Long id;
    private String name;
    private String logoUrl;
    private String industry;
    private String location;
}
