package com.ttjobs.backend.controller;

import com.ttjobs.backend.dto.JobNeedPreferenceDTO;
import com.ttjobs.backend.dto.JobNeedPreferenceRequest;
import com.ttjobs.backend.service.JobNeedPreferenceService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/job-needs")
public class JobNeedPreferenceController {

    @Autowired
    private JobNeedPreferenceService jobNeedPreferenceService;

    @GetMapping("/preferences")
    public JobNeedPreferenceDTO getMyPreferences() {
        return jobNeedPreferenceService.getMyPreferences();
    }

    @PutMapping("/preferences")
    public JobNeedPreferenceDTO updateMyPreferences(@Valid @RequestBody JobNeedPreferenceRequest request) {
        return jobNeedPreferenceService.updateMyPreferences(request);
    }
}
