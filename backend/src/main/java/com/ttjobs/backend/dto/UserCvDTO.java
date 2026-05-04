package com.ttjobs.backend.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UserCvDTO {
    private Long id;
    private Long userId;
    private String cvUrl;
    private String fileName;
    private LocalDateTime uploadedAt;
    private Boolean current;
}
