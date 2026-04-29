package com.ttjobs.backend.service;

import com.ttjobs.backend.entity.Company;
import com.ttjobs.backend.entity.CompanyMember;
import com.ttjobs.backend.entity.User;
import com.ttjobs.backend.repository.CompanyMemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class CompanyAuthorizationService {

    @Autowired
    private AuthContextService authContextService;

    @Autowired
    private CompanyMemberRepository companyMemberRepository;

    public boolean canManageCompany(User user, Company company) {
        if (authContextService.isAdmin(user)) {
            return true;
        }
        if (company == null || company.getDeletedAt() != null || user == null) {
            return false;
        }
        if (company.getCreatedBy() != null && user.getId().equals(company.getCreatedBy().getId())) {
            return true;
        }
        return companyMemberRepository.existsByCompanyIdAndUserIdAndMemberRoleIn(
                company.getId(),
                user.getId(),
                List.of(CompanyMember.MemberRole.RECRUITER, CompanyMember.MemberRole.ADMIN)
        );
    }

    public boolean canAdministerCompany(User user, Company company) {
        if (authContextService.isAdmin(user)) {
            return true;
        }
        if (company == null || company.getDeletedAt() != null || user == null) {
            return false;
        }
        if (company.getCreatedBy() != null && user.getId().equals(company.getCreatedBy().getId())) {
            return true;
        }
        return companyMemberRepository.existsByCompanyIdAndUserIdAndMemberRoleIn(
                company.getId(),
                user.getId(),
                List.of(CompanyMember.MemberRole.ADMIN)
        );
    }

    public void requireManageCompany(User user, Company company) {
        if (!canManageCompany(user, company)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not have permission for this company");
        }
    }

    public void requireAdministerCompany(User user, Company company) {
        if (!canAdministerCompany(user, company)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not have admin permission for this company");
        }
    }
}
