package com.ttjobs.backend.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "companies")
@Data
public class Company {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String name;
    private String description;
    private String location;
    private String website;
    private String industry;

    @OneToMany(mappedBy = "company", cascade = CascadeType.PERSIST)
    private List<Job> jobs;
    @ManyToOne
    @JoinColumn(name = "created_by")
    private User createdBy;
    private String logoUrl;
    private LocalDateTime createdAt;
}