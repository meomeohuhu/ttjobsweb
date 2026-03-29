package com.job.backend.resume.controller;

import com.job.backend.resume.entity.Resume;
import com.job.backend.resume.service.ResumeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/resumes")
@RequiredArgsConstructor
public class ResumeController {

    private final ResumeService resumeService;

    // UPLOAD FILE
    @PostMapping("/upload")
    public Resume uploadResume(@RequestParam("file") MultipartFile file) throws IOException {
        String email = "test@gmail.com"; // tạm thời
        return resumeService.uploadResume(email, file);
    }

    // LẤY CV CỦA USER
    @GetMapping("/my")
    public List<Resume> getMyResumes() {
        String email = "test@gmail.com";
        return resumeService.getMyResumes(email);
    }

    // XOÁ CV
    @DeleteMapping("/{id}")
    public String deleteResume(@PathVariable Long id) {
        resumeService.deleteResume(id);
        return "Deleted successfully";
    }
}