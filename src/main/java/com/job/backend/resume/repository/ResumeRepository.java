package com.job.backend.resume.repository;

import com.job.backend.resume.entity.Resume;
import com.job.backend.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ResumeRepository extends JpaRepository<Resume, Long> {

    List<Resume> findByUser(User user);
}