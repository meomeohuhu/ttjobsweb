package com.ttjobs.backend.dto.ai;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;

@Data
public class AiSignalDTO {
    private Long id;
    private Long ownerId;
    private String normalizedTitle;
    private String seniority;
    private List<String> skills;
    private List<String> industries;
    private List<String> locations;
    private BigDecimal salaryMin;
    private BigDecimal salaryMax;
    private String currency;
    private List<String> languages;
    private List<String> evidence;
    private String source;
    private LocalDateTime updatedAt;
}
