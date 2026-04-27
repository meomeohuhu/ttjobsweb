package com.ttjobs.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Entity
@Table(name = "recruitment_campaigns")
@Data
public class RecruitmentCampaign {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "company_id")
    private Company company;

    @ManyToOne
    @JoinColumn(name = "created_by_id")
    private User createdBy;

    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String status;
    private Integer targetHires;
    private LocalDateTime startsAt;
    private LocalDateTime endsAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @ManyToMany
    @JoinTable(
            name = "recruitment_campaign_jobs",
            joinColumns = @JoinColumn(name = "campaign_id"),
            inverseJoinColumns = @JoinColumn(name = "job_id")
    )
    private List<Job> jobs = new ArrayList<>();

    @ManyToMany
    @JoinTable(
            name = "recruitment_campaign_applications",
            joinColumns = @JoinColumn(name = "campaign_id"),
            inverseJoinColumns = @JoinColumn(name = "application_id")
    )
    private List<JobApplication> applications = new ArrayList<>();

    @PrePersist
    public void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
        if (status == null || status.isBlank()) {
            status = "active";
        }
    }

    @PreUpdate
    public void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
