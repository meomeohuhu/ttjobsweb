package com.job.backend.resume.service;

import com.job.backend.resume.entity.Resume;
import com.job.backend.resume.repository.ResumeRepository;
import com.job.backend.user.entity.User;
import com.job.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ResumeService {

    private final ResumeRepository resumeRepository;
    private final UserRepository userRepository;

    private final String UPLOAD_DIR = "uploads/";

    // UPLOAD CV
    public Resume uploadResume(String email, MultipartFile file) throws IOException {

        if (file.isEmpty()) {
            throw new RuntimeException("File is empty");
        }

        String fileName = file.getOriginalFilename();

        if (fileName == null ||
                (!fileName.endsWith(".pdf") && !fileName.endsWith(".docx"))) {
            throw new RuntimeException("Only PDF or DOCX allowed");
        }

        // tạo tên file unique
        String newFileName = UUID.randomUUID() + "_" + fileName;

        File uploadFolder = new File(UPLOAD_DIR);
        if (!uploadFolder.exists()) {
            uploadFolder.mkdirs();
        }

        File saveFile = new File(UPLOAD_DIR + newFileName);
        file.transferTo(saveFile);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Resume resume = new Resume();
        resume.setFileUrl("/uploads/" + newFileName);
        resume.setUploadedAt(LocalDateTime.now());
        resume.setUser(user);

        return resumeRepository.save(resume);
    }

    // LẤY CV CỦA USER
    public List<Resume> getMyResumes(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return resumeRepository.findByUser(user);
    }

    // DELETE
    public void deleteResume(Long id) {
        resumeRepository.deleteById(id);
    }
}