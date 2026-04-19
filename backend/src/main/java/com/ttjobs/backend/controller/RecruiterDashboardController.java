package com.ttjobs.backend.controller;

import com.ttjobs.backend.dto.RecruiterDashboardDTO;
import com.ttjobs.backend.service.RecruiterDashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/recruiter")
public class RecruiterDashboardController {

    @Autowired
    private RecruiterDashboardService recruiterDashboardService;

    @GetMapping("/dashboard")
    public RecruiterDashboardDTO getDashboard() {
        return recruiterDashboardService.getDashboard();
    }
}
