package com.ttjobs.backend.repository;

import com.ttjobs.backend.entity.Company;
import com.ttjobs.backend.entity.Job;
import com.ttjobs.backend.entity.Skill;
import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;

public final class JobSpecifications {

    private JobSpecifications() {
    }

    public static Specification<Job> activeJobs() {
        return (root, query, cb) -> {
            query.distinct(true);
            Join<Job, Company> company = root.join("company", JoinType.INNER);
            return cb.and(
                    cb.isNull(root.get("deletedAt")),
                    cb.isNull(company.get("deletedAt"))
            );
        };
    }

    public static Specification<Job> keywordLike(String keyword) {
        return (root, query, cb) -> {
            String pattern = "%" + keyword.toLowerCase(Locale.ROOT) + "%";
            Join<Job, Company> company = root.join("company", JoinType.LEFT);
            return cb.or(
                    cb.like(cb.lower(root.get("title")), pattern),
                    cb.like(cb.lower(root.get("description")), pattern),
                    cb.like(cb.lower(company.get("name")), pattern)
            );
        };
    }

    public static Specification<Job> locationLike(String location) {
        return (root, query, cb) ->
                cb.like(cb.lower(root.get("location")), "%" + location.toLowerCase(Locale.ROOT) + "%");
    }

    public static Specification<Job> companyNameLike(String companyName) {
        return (root, query, cb) -> {
            Join<Job, Company> company = root.join("company", JoinType.LEFT);
            return cb.like(cb.lower(company.get("name")), "%" + companyName.toLowerCase(Locale.ROOT) + "%");
        };
    }

    public static Specification<Job> jobTypeEquals(String jobType) {
        return (root, query, cb) -> cb.equal(root.get("jobType"), jobType);
    }

    public static Specification<Job> experienceLevelEquals(String experienceLevel) {
        return (root, query, cb) -> cb.equal(root.get("experienceLevel"), experienceLevel);
    }

    public static Specification<Job> statusEquals(String status) {
        return (root, query, cb) -> cb.equal(root.get("status"), status);
    }

    public static Specification<Job> salaryMinGte(BigDecimal min) {
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("salaryMax"), min);
    }

    public static Specification<Job> salaryMaxLte(BigDecimal max) {
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("salaryMin"), max);
    }

    public static Specification<Job> hasAnySkill(List<String> skills) {
        return (root, query, cb) -> {
            Join<Job, Skill> skillJoin = root.join("skills", JoinType.LEFT);
            List<String> normalized = skills.stream()
                    .filter(s -> s != null && !s.isBlank())
                    .map(s -> s.trim().toLowerCase(Locale.ROOT))
                    .toList();
            if (normalized.isEmpty()) {
                return cb.conjunction();
            }
            return cb.lower(skillJoin.get("name")).in(normalized);
        };
    }

    public static Specification<Job> categoryIn(List<String> categories) {
        return (root, query, cb) -> {
            if (categories == null || categories.isEmpty()) {
                return cb.conjunction();
            }
            List<String> normalized = categories.stream()
                    .filter(c -> c != null && !c.isBlank())
                    .map(c -> c.trim().toLowerCase(Locale.ROOT))
                    .toList();
            if (normalized.isEmpty()) {
                return cb.conjunction();
            }
            return cb.lower(root.get("category")).in(normalized);
        };
    }

}
