package com.ttjobs.backend.controller;

import com.ttjobs.backend.dto.SavedJobDTO;
import com.ttjobs.backend.dto.SavedJobNoteRequest;
import com.ttjobs.backend.service.SavedJobService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

    @PatchMapping("/{jobId}/note")
    public SavedJobDTO updateNote(@PathVariable Long jobId, @Valid @RequestBody SavedJobNoteRequest request) {
        return savedJobService.updateNote(jobId, request);
    }
}
