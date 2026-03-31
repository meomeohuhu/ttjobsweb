package com.ttjobs.backend.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;
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
    private enum Status{
        pending,
        accepted,
        rejected
    } // PENDING, ACCEPTED, REJECTED

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    

    @ManyToOne
    @JoinColumn(name = "job_id")
    private Job job;
}