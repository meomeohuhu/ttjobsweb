package com.ttjobs.backend.dto.savedsearch;

import java.math.BigDecimal;
import java.util.List;
import lombok.Data;

@Data
public class SavedSearchRequest {
    private String name;
    private String keyword;
    private String location;
    private String category;
    private String jobType;
    private String experienceLevel;
    private BigDecimal salaryMin;
    private BigDecimal salaryMax;
    private Boolean remoteOnly;
    private List<String> skills;
    private String alertFrequency;
    private Boolean active;
}
