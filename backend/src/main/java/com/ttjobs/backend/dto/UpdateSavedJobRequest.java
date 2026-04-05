package com.ttjobs.backend.dto;

import lombok.Data;

@Data
public class UpdateSavedJobRequest {
    private String note;
    private String tag;
}
