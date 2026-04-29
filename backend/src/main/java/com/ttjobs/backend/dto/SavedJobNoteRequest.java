package com.ttjobs.backend.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SavedJobNoteRequest {
    @Size(max = 2000, message = "note must be at most 2000 characters")
    private String note;

    @Size(max = 100, message = "tag must be at most 100 characters")
    private String tag;
}
