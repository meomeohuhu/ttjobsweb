package com.ttjobs.backend.repository;

import com.ttjobs.backend.entity.EmailChangeVerification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmailChangeVerificationRepository extends JpaRepository<EmailChangeVerification, Long> {
    Optional<EmailChangeVerification> findTopByUserIdAndNewEmailAndUsedAtIsNullOrderByCreatedAtDesc(Long userId, String newEmail);
    Optional<EmailChangeVerification> findTopByUserIdOrderByCreatedAtDesc(Long userId);
}
