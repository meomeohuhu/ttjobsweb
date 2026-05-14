package com.ttjobs.backend.controller;

import com.ttjobs.backend.dto.tool.ToolSessionRequest;
import com.ttjobs.backend.dto.tool.ToolSessionResponse;
import com.ttjobs.backend.service.ToolSessionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/tools/sessions")
public class ToolSessionController {

    @Autowired
    private ToolSessionService toolSessionService;

    @PostMapping
    public ToolSessionResponse saveSession(@Valid @RequestBody ToolSessionRequest request) {
        return toolSessionService.saveSession(request);
    }

    @GetMapping
    public List<ToolSessionResponse> getMySessions(@RequestParam(required = false) String toolSlug) {
        return toolSessionService.getMySessions(toolSlug);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteMySession(@PathVariable Long id) {
        toolSessionService.deleteMySession(id);
    }
}

