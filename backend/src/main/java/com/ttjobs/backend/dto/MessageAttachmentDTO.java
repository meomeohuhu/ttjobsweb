package com.ttjobs.backend.dto;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class MessageAttachmentDTO {
    private Long id;
    private String fileName;
    private String fileUrl;
    private String publicId;
    private String mimeType;
    private Long fileSize;
    private LocalDateTime createdAt;
}
