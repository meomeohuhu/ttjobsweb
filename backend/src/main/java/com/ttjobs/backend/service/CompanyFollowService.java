package com.ttjobs.backend.service;

import com.ttjobs.backend.dto.CompanyFollowStatusDTO;
import com.ttjobs.backend.dto.CompanyDTO;
import com.ttjobs.backend.entity.Company;
import com.ttjobs.backend.entity.CompanyFollow;
import com.ttjobs.backend.entity.Job;
import com.ttjobs.backend.entity.User;
import com.ttjobs.backend.repository.CompanyFollowRepository;
import com.ttjobs.backend.repository.CompanyRepository;
import com.ttjobs.backend.repository.JobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class CompanyFollowService {

    @Autowired
    private CompanyFollowRepository companyFollowRepository;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private NotificationService notificationService;

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

    public List<CompanyDTO> getMyFollowedCompanies() {
        User currentUser = requireCandidate();
        return companyFollowRepository.findByUserIdOrderByFollowedAtDesc(currentUser.getId())
                .stream()
                .filter(follow -> follow.getCompany() != null && follow.getCompany().getDeletedAt() == null)
                .map(follow -> toCompanyDto(follow.getCompany()))
                .toList();
    }

    public void notifyFollowersAboutNewJob(Job job) {
        if (job == null || job.getCompany() == null || job.getCompany().getId() == null) {
            return;
        }
        if (job.getStatus() == null || !"open".equalsIgnoreCase(job.getStatus())) {
            return;
        }
        String title = "Công ty bạn theo dõi vừa đăng việc mới";
        String content = job.getCompany().getName() + " đang tuyển " + job.getTitle() + ".";
        String targetUrl = "/jobs/" + job.getId();
        companyFollowRepository.findByCompanyId(job.getCompany().getId()).forEach(follow -> {
            notificationService.createNotification(follow.getUser(), title, content, "company_job", targetUrl);
        });
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

    private CompanyDTO toCompanyDto(Company company) {
        CompanyDTO dto = new CompanyDTO();
        dto.setId(company.getId());
        dto.setName(company.getName());
        dto.setDescription(company.getDescription());
        dto.setLocation(company.getLocation());
        dto.setWebsite(company.getWebsite());
        dto.setIndustry(company.getIndustry());
        dto.setLogoUrl(company.getLogoUrl());
        dto.setFollowerCount(companyFollowRepository.countByCompanyId(company.getId()));
        dto.setJobCount(jobRepository.countByCompanyIdAndStatus(company.getId(), "open"));
        dto.setSavedJobCount(jobRepository.countSavedJobsByCompanyId(company.getId()));
        return dto;
    }
}
