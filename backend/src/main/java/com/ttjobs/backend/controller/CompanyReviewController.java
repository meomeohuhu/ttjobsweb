package com.ttjobs.backend.controller;

import com.ttjobs.backend.dto.CompanyReviewDTO;
import com.ttjobs.backend.dto.CompanyReviewRequest;
import com.ttjobs.backend.dto.CompanyReviewSummaryDTO;
import com.ttjobs.backend.service.CompanyReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/companies/{companyId}/reviews")
public class CompanyReviewController {

    @Autowired
    private CompanyReviewService companyReviewService;

    @GetMapping
    public CompanyReviewSummaryDTO getReviews(@PathVariable Long companyId) {
        return companyReviewService.getCompanyReviews(companyId);
    }

    @PostMapping
    public CompanyReviewDTO createReview(@PathVariable Long companyId, @RequestBody CompanyReviewRequest request) {
        return companyReviewService.createReview(companyId, request);
    }
}
