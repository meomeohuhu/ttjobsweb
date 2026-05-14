package com.ttjobs.backend.dto.savedsearch;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;

@Data
public class SavedSearchDTO {
    private Long id;
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
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
