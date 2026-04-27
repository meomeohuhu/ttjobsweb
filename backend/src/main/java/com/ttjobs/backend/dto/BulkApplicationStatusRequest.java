package com.ttjobs.backend.dto;

import java.util.List;
import lombok.Data;

@Data
public class BulkApplicationStatusRequest {
    private List<Long> applicationIds;
    private String status;
}
