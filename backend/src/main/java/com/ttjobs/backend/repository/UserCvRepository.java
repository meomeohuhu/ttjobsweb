package com.ttjobs.backend.repository;

import com.ttjobs.backend.entity.UserCv;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserCvRepository extends JpaRepository<UserCv, Long> {
    Optional<UserCv> findTopByUserIdOrderByUploadedAtDesc(Long userId);
}
