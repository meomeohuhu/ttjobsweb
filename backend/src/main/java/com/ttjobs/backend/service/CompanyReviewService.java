package com.ttjobs.backend.service;

import com.ttjobs.backend.dto.CompanyReviewDTO;
import com.ttjobs.backend.dto.CompanyReviewRequest;
import com.ttjobs.backend.dto.CompanyReviewSummaryDTO;
import com.ttjobs.backend.entity.Company;
import com.ttjobs.backend.entity.CompanyReview;
import com.ttjobs.backend.entity.User;
import com.ttjobs.backend.repository.CompanyRepository;
import com.ttjobs.backend.repository.CompanyReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Objects;

@Service
public class CompanyReviewService {

    @Autowired
    private CompanyReviewRepository companyReviewRepository;
    @Autowired
    private CompanyRepository companyRepository;
    @Autowired
    private AuthContextService authContextService;

    public CompanyReviewSummaryDTO getCompanyReviews(Long companyId) {
        requireCompany(companyId);
        List<CompanyReviewDTO> reviews = companyReviewRepository.findByCompanyIdOrderByCreatedAtDesc(companyId)
                .stream()
                .map(this::toDto)
                .toList();

        CompanyReviewSummaryDTO summary = new CompanyReviewSummaryDTO();
        summary.setCompanyId(companyId);
        summary.setReviews(reviews);
        summary.setReviewCount((long) reviews.size());
        summary.setAverageRating(reviews.stream()
                .map(CompanyReviewDTO::getRating)
                .filter(Objects::nonNull)
                .mapToInt(Integer::intValue)
                .average()
                .orElse(0.0));
        List<BigDecimal> salaries = reviews.stream()
                .map(CompanyReviewDTO::getSalary)
                .filter(Objects::nonNull)
                .toList();
        if (!salaries.isEmpty()) {
            BigDecimal total = salaries.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
            summary.setAverageSalary(total.divide(BigDecimal.valueOf(salaries.size()), 2, RoundingMode.HALF_UP));
        }
        return summary;
    }

    public CompanyReviewDTO createReview(Long companyId, CompanyReviewRequest request) {
        User currentUser = authContextService.requireCurrentUser();
        Company company = requireCompany(companyId);
        if (request == null || request.getRating() == null || request.getRating() < 1 || request.getRating() > 5) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "rating must be between 1 and 5");
        }

        CompanyReview review = new CompanyReview();
        review.setCompany(company);
        review.setUser(currentUser);
        review.setRating(request.getRating());
        review.setPros(trimToNull(request.getPros()));
        review.setCons(trimToNull(request.getCons()));
        review.setSalary(request.getSalary());
        review.setAnonymous(request.getAnonymous() == null || request.getAnonymous());
        return toDto(companyReviewRepository.save(review));
    }

    private Company requireCompany(Long companyId) {
        return companyRepository.findByIdAndDeletedAtIsNull(companyId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Company not found"));
    }

    private CompanyReviewDTO toDto(CompanyReview review) {
        CompanyReviewDTO dto = new CompanyReviewDTO();
        dto.setId(review.getId());
        if (review.getCompany() != null) {
            dto.setCompanyId(review.getCompany().getId());
            dto.setCompanyName(review.getCompany().getName());
        }
        dto.setRating(review.getRating());
        dto.setPros(review.getPros());
        dto.setCons(review.getCons());
        dto.setSalary(review.getSalary());
        dto.setAnonymous(review.getAnonymous());
        dto.setReviewerName(Boolean.TRUE.equals(review.getAnonymous()) || review.getUser() == null
                ? "Ẩn danh"
                : review.getUser().getName());
        dto.setCreatedAt(review.getCreatedAt());
        return dto;
    }

    private String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}

