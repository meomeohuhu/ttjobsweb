package com.ttjobs.backend.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "job_applications")
@Data
public class JobApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime applicationDate;
    private String status;
    private LocalDateTime updatedAt;
    // Snapshot CV info at apply time (do not expose URL via DTO).
    private String cvUrl;
    private String cvFileName;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    // Optional link to a saved CV in the user's CV list.
    @ManyToOne
    @JoinColumn(name = "cv_id")
    private UserCv cv;

    @ManyToOne
    @JoinColumn(name = "job_id")
    private Job job;

    @PrePersist
    public void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (applicationDate == null) {
            applicationDate = now;
        }
        updatedAt = now;
    }

    @PreUpdate
    public void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
