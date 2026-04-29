package com.ttjobs.backend.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "job_application_status_audits")
@Data
public class JobApplicationStatusAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "application_id")
    private JobApplication application;

    @ManyToOne
    @JoinColumn(name = "changed_by_id")
    private User changedBy;

    private String fromStatus;
    private String toStatus;
    private LocalDateTime changedAt;
}
