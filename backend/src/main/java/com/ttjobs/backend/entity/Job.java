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
    private String jobType;
    private enum JobType {
        FULL_TIME,
        PART_TIME,
        CONTRACT,
        INTERN,
        REMOTE
    }
    private String experienceLevel; // ENTRY, MID, SENIOR, etc.
    private LocalDateTime postedDate;
    private LocalDateTime applicationDeadline;

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
}