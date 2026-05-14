package com.ttjobs.backend.dto.common;

import lombok.Data;

@Data
public class AdminActionRequest {
    private String reason;
    private String note;
    private Integer days;
}
