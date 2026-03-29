package com.job.backend.resume.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class ResumeResponse {
    private Long id;
    private String fileUrl;
    private LocalDateTime createdAt;
}