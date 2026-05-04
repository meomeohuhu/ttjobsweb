package com.ttjobs.backend.entity;

import java.time.LocalDateTime;
import java.util.List;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import lombok.Data;

@Entity
@Table(name = "users")
@Data
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    // Unique and non-nullable email 
    @Column(unique = true,nullable = false)
    private String email;
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    public enum Role {
        CANDIDATE,
        RECRUITER,
        ADMIN
    }

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    private String phone;
    private String address;
    @ManyToMany
    @JoinTable(
        name="user_skills",
        joinColumns =@JoinColumn(name="user_id"),
        inverseJoinColumns = @JoinColumn(name="skill_id")
    )
    private List<Skill> skills;
    private Integer experienceYears;
    private String cvUrl;
    private String cvText;
    private String cvRole;
    @Column(columnDefinition = "TEXT")
    private String cvObjective;
    @Column(columnDefinition = "TEXT")
    private String cvExperienceHighlights;

    public enum PrimaryCvType {
        BUILDER,
        UPLOADED
    }

    @Enumerated(EnumType.STRING)
    private PrimaryCvType primaryCvType;

    private String avatarUrl;
    private String mbtiType;
    private LocalDateTime mbtiTakenAt;
    @Column(columnDefinition = "TEXT")
    private String miScoresJson;
    private LocalDateTime miTakenAt;
    private Boolean personalityPublic = false;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    

}
