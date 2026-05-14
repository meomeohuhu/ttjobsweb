package com.ttjobs.backend.dto.job;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class JobCategoryStatDTO {
    private String category;
    private String label;
    private Long jobCount;
}

