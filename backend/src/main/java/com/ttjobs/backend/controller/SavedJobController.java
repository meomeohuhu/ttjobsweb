package com.ttjobs.backend.controller;

import com.ttjobs.backend.dto.SavedJobDTO;
import com.ttjobs.backend.dto.UpdateSavedJobRequest;
import com.ttjobs.backend.service.SavedJobService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@RestController
@RequestMapping("/api/saved-jobs")
public class SavedJobController {

    @Autowired
    private SavedJobService savedJobService;

    @GetMapping
    public List<SavedJobDTO> getMySavedJobs() {
        return savedJobService.getMySavedJobs();
    }

    @PostMapping("/{jobId}")
    public SavedJobDTO saveJob(@PathVariable Long jobId) {
        return savedJobService.saveJob(jobId);
    }

    @DeleteMapping("/{jobId}")
    public void unsaveJob(@PathVariable Long jobId) {
        savedJobService.unsaveJob(jobId);
    }

    @PutMapping("/{jobId}")
    public SavedJobDTO updateSavedJob(@PathVariable Long jobId, @RequestBody UpdateSavedJobRequest request) {
        return savedJobService.updateSavedJob(jobId, request.getNote(), request.getTag());
    }
}
