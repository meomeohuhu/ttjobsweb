package com.ttjobs.backend.dto;

import lombok.Data;

@Data
public class EmailChangeResponse {
    private String email;
    private String token;
}
