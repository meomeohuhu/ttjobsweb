package com.ttjobs.backend.dto.auth;

import lombok.Data;

@Data
public class EmailChangeResponse {
    private String email;
    private String token;
}

