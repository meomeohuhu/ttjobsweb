package com.ttjobs.backend.service;

import com.ttjobs.backend.dto.CompanyFollowStatusDTO;
import com.ttjobs.backend.entity.Company;
import com.ttjobs.backend.entity.CompanyFollow;
import com.ttjobs.backend.entity.User;
import com.ttjobs.backend.repository.CompanyFollowRepository;
import com.ttjobs.backend.repository.CompanyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class CompanyFollowService {

    @Autowired
    private CompanyFollowRepository companyFollowRepository;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private AuthContextService authContextService;

    public CompanyFollowStatusDTO followCompany(Long companyId) {
        User currentUser = requireCandidate();
        Company company = requireCompany(companyId);

        if (companyFollowRepository.existsByUserIdAndCompanyId(currentUser.getId(), companyId)) {
            return getFollowStatus(companyId);
        }

        CompanyFollow follow = new CompanyFollow();
        follow.setUser(currentUser);
        follow.setCompany(company);
        companyFollowRepository.save(follow);
        return getFollowStatus(companyId);
    }

    public CompanyFollowStatusDTO unfollowCompany(Long companyId) {
        User currentUser = requireCandidate();
        requireCompany(companyId);

        companyFollowRepository.findByUserIdAndCompanyId(currentUser.getId(), companyId)
                .ifPresent(companyFollowRepository::delete);

        return getFollowStatus(companyId);
    }

    public CompanyFollowStatusDTO getFollowStatus(Long companyId) {
        requireCompany(companyId);
        User currentUser = authContextService.getCurrentUserOptional().orElse(null);

        CompanyFollowStatusDTO dto = new CompanyFollowStatusDTO();
        dto.setCompanyId(companyId);
        dto.setFollowerCount(companyFollowRepository.countByCompanyId(companyId));
        dto.setFollowed(currentUser != null
                && companyFollowRepository.existsByUserIdAndCompanyId(currentUser.getId(), companyId));
        return dto;
    }

    private User requireCandidate() {
        User currentUser = authContextService.requireCurrentUser();
        if (currentUser.getRole() != User.Role.CANDIDATE) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only candidate can follow companies");
        }
        return currentUser;
    }

    private Company requireCompany(Long companyId) {
        return companyRepository.findByIdAndDeletedAtIsNull(companyId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Company not found"));
    }
}
