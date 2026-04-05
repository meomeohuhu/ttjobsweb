package com.ttjobs.backend.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "jobs")
@Data
public class Job {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String title;
    private String description;
    private String location;
    private BigDecimal salary;
    private BigDecimal salaryMin;
    private BigDecimal salaryMax;
    @Column(nullable = false)
    private String currency;
    private String jobType;
    private String experienceLevel; // ENTRY, MID, SENIOR, etc.
    private String category;
    @Column(nullable = false)
    private String status;
    private LocalDateTime postedDate;
    private LocalDateTime applicationDeadline;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;

    @ManyToOne
    @JoinColumn(name = "company_id")
    private Company company;

    @OneToMany(mappedBy = "job", cascade = CascadeType.PERSIST)
    private List<JobApplication> applications;
    @ManyToMany
    @JoinTable(
        name="job_skills",
        joinColumns = @JoinColumn(name="job_id"),
        inverseJoinColumns = @JoinColumn(name="skill_id")
    )
    private List<Skill> skills;

    @PrePersist
    public void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (postedDate == null) {
            postedDate = now;
        }
        updatedAt = now;
    }

    @PreUpdate
    public void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
