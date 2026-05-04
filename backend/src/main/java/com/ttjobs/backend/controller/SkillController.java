package com.ttjobs.backend.controller;

import com.ttjobs.backend.dto.CreateSkillRequest;
import com.ttjobs.backend.dto.SkillDTO;
import com.ttjobs.backend.service.SkillService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class SkillController {

    @Autowired
    private SkillService skillService;

    @GetMapping("/skills")
    public List<SkillDTO> searchSkills(@RequestParam(required = false) String q) {
        return skillService.searchSkills(q);
    }

    @PostMapping("/admin/skills")
    public SkillDTO createSkill(@Valid @RequestBody CreateSkillRequest request) {
        return skillService.createSkill(request);
    }
}
