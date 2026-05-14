package com.ttjobs.backend.controller;

import com.ttjobs.backend.dto.job.JobDTO;
import com.ttjobs.backend.dto.savedsearch.SavedSearchDTO;
import com.ttjobs.backend.dto.savedsearch.SavedSearchRequest;
import com.ttjobs.backend.service.SavedSearchService;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/saved-searches")
public class SavedSearchController {

    @Autowired
    private SavedSearchService savedSearchService;

    @GetMapping
    public List<SavedSearchDTO> getMine() {
        return savedSearchService.getMine();
    }

    @PostMapping
    public SavedSearchDTO create(@RequestBody SavedSearchRequest request) {
        return savedSearchService.create(request);
    }

    @PutMapping("/{id}")
    public SavedSearchDTO update(@PathVariable Long id, @RequestBody SavedSearchRequest request) {
        return savedSearchService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        savedSearchService.delete(id);
    }

    @PostMapping("/{id}/run")
    public List<JobDTO> run(@PathVariable Long id) {
        return savedSearchService.run(id);
    }

    @PostMapping("/alerts/run")
    public Map<String, Integer> runAlerts() {
        return Map.of("notifiedCount", savedSearchService.runAlerts());
    }
}
