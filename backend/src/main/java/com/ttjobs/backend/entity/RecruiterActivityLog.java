package com.ttjobs.backend.entity;

import jakarta.persistence.Column;
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
@Table(name = "recruiter_activity_logs")
@Data
public class RecruiterActivityLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "actor_id", nullable = false)
    private User actor;

    @ManyToOne
    @JoinColumn(name = "company_id")
    private Company company;

    @ManyToOne
    @JoinColumn(name = "job_id")
    private Job job;

    @ManyToOne
    @JoinColumn(name = "application_id")
    private JobApplication application;

    @Column(nullable = false)
    private String actionType;

    @Column(nullable = false)
    private String title;

    @Column(length = 1000)
    private String details;

    @Column(nullable = false)
    private LocalDateTime createdAt;
}
