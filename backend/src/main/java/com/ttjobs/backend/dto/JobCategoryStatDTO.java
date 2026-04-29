package com.ttjobs.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class JobCategoryStatDTO {
    private String category;
    private String label;
    private Long jobCount;
}
