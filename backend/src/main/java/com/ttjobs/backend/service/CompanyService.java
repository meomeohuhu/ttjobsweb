package com.ttjobs.backend.service;

import com.ttjobs.backend.dto.CompanyDTO;
import com.ttjobs.backend.dto.CompanyMemberDTO;
import com.ttjobs.backend.dto.CompanyMemberUpsertRequest;
import com.ttjobs.backend.dto.CompanyPublicPageDTO;
import com.ttjobs.backend.dto.JobDTO;
import com.ttjobs.backend.entity.Company;
import com.ttjobs.backend.entity.CompanyMember;
import com.ttjobs.backend.entity.Job;
import com.ttjobs.backend.entity.User;
import com.ttjobs.backend.repository.CompanyMemberRepository;
import com.ttjobs.backend.repository.CompanyFollowRepository;
import com.ttjobs.backend.repository.CompanyRepository;
import com.ttjobs.backend.repository.JobRepository;
import com.ttjobs.backend.repository.JobWithSavedCount;
import com.ttjobs.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CompanyService {

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private CompanyMemberRepository companyMemberRepository;

    @Autowired
    private CompanyFollowRepository companyFollowRepository;

    @Autowired
    private AuthContextService authContextService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CompanyAuthorizationService companyAuthorizationService;

    @Autowired
    private RecruiterActivityLogService recruiterActivityLogService;
    @Autowired
    private ImageUploadService imageUploadService;

    @Autowired
    private JobRepository jobRepository;

    public List<CompanyDTO> getAllCompanies() {
        // Public company listing for authenticated users.
        return companyRepository.findByDeletedAtIsNull().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public Optional<CompanyDTO> getCompanyById(Long id) {
        return companyRepository.findByIdAndDeletedAtIsNull(id).map(this::convertToDTO);
    }

    public List<CompanyDTO> getTopCompaniesBySavedJobs(int limit) {
        int safeLimit = Math.max(limit, 1);
        Comparator<CompanyDTO> comparator = Comparator
                .comparingLong(CompanyDTO::getSavedJobCount)
                .reversed()
                .thenComparing(Comparator.comparingLong(CompanyDTO::getJobCount).reversed())
                .thenComparing(company -> company.getName() == null ? "" : company.getName().toLowerCase());

        return companyRepository.findByDeletedAtIsNull().stream()
                .map(this::convertToDTO)
                .sorted(comparator)
                .limit(safeLimit)
                .collect(Collectors.toList());
    }

    public List<JobDTO> getPublicCompanyJobs(Long companyId) {
        Company company = companyRepository.findByIdAndDeletedAtIsNull(companyId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Company not found"));

        Pageable pageable = PageRequest.of(0, 20);
        return jobRepository.findCompanyJobsWithSavedCount(company.getId(), "open", pageable).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public CompanyPublicPageDTO getPublicCompanyPage(Long companyId) {
        Company company = companyRepository.findByIdAndDeletedAtIsNull(companyId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Company not found"));

        Pageable pageable = PageRequest.of(0, 20);
        CompanyPublicPageDTO dto = new CompanyPublicPageDTO();
        dto.setCompany(convertToDTO(company));
        dto.setJobs(jobRepository.findCompanyJobsWithSavedCount(company.getId(), "open", pageable).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList()));
        return dto;
    }

    public CompanyDTO createCompany(Company company) {
        User currentUser = authContextService.requireCurrentUser();
        requireRecruiterOrAdmin(currentUser);

        // Force ownership to current user to prevent privilege injection.
        company.setCreatedBy(currentUser);
        if (company.getCreatedAt() == null) {
            company.setCreatedAt(LocalDateTime.now());
        }

        Company savedCompany = companyRepository.save(company);

        // Keep creator as company admin member for future multi-recruiter authorization.
        if (!companyMemberRepository.existsByCompanyIdAndUserId(savedCompany.getId(), currentUser.getId())) {
            CompanyMember member = new CompanyMember();
            member.setCompany(savedCompany);
            member.setUser(currentUser);
            member.setMemberRole(CompanyMember.MemberRole.ADMIN);
            companyMemberRepository.save(member);
        }

        if (recruiterActivityLogService != null) {
            recruiterActivityLogService.logCompanyCreated(currentUser, savedCompany);
        }
        return convertToDTO(savedCompany);
    }

    public CompanyDTO updateCompany(Long id, Company companyDetails) {
        User currentUser = authContextService.requireCurrentUser();

        Company company = companyRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Company not found"));

        requireCompanyManagePermission(currentUser, company);

        if (companyDetails.getName() != null) {
            company.setName(companyDetails.getName());
        }
        if (companyDetails.getDescription() != null) {
            company.setDescription(companyDetails.getDescription());
        }
        if (companyDetails.getLocation() != null) {
            company.setLocation(companyDetails.getLocation());
        }
        if (companyDetails.getWebsite() != null) {
            company.setWebsite(companyDetails.getWebsite());
        }
        if (companyDetails.getIndustry() != null) {
            company.setIndustry(companyDetails.getIndustry());
        }
        if (companyDetails.getLogoUrl() != null) {
            company.setLogoUrl(companyDetails.getLogoUrl());
        }

        Company saved = companyRepository.save(company);
        if (recruiterActivityLogService != null) {
            recruiterActivityLogService.logCompanyUpdated(currentUser, saved);
        }
        return convertToDTO(saved);
    }

    public void deleteCompany(Long id) {
        User currentUser = authContextService.requireCurrentUser();

        Company company = companyRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Company not found"));

        requireCompanyManagePermission(currentUser, company);
        company.setDeletedAt(LocalDateTime.now());
        Company saved = companyRepository.save(company);
        if (recruiterActivityLogService != null) {
            recruiterActivityLogService.logCompanyDeleted(currentUser, saved);
        }
    }

    public CompanyDTO uploadCompanyLogo(Long id, org.springframework.web.multipart.MultipartFile file) {
        User currentUser = authContextService.requireCurrentUser();
        Company company = companyRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Company not found"));

        requireCompanyManagePermission(currentUser, company);
        String logoUrl = imageUploadService.uploadImage(file, "ttjobs/companies", "company-" + company.getId());
        company.setLogoUrl(logoUrl);
        Company saved = companyRepository.save(company);
        if (recruiterActivityLogService != null) {
            recruiterActivityLogService.logCompanyUpdated(currentUser, saved);
        }
        return convertToDTO(saved);
    }

    public List<CompanyMemberDTO> getCompanyMembers(Long companyId) {
        User currentUser = authContextService.requireCurrentUser();
        Company company = companyRepository.findByIdAndDeletedAtIsNull(companyId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Company not found"));

        requireCompanyManagePermission(currentUser, company);

        return companyMemberRepository.findByCompanyId(companyId).stream()
                .map(this::convertToMemberDTO)
                .collect(Collectors.toList());
    }

    public CompanyMemberDTO addCompanyMember(Long companyId, CompanyMemberUpsertRequest request) {
        User currentUser = authContextService.requireCurrentUser();
        Company company = companyRepository.findByIdAndDeletedAtIsNull(companyId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Company not found"));

        requireCompanyAdminPermission(currentUser, company);

        User targetUser = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (targetUser.getRole() != User.Role.RECRUITER && targetUser.getRole() != User.Role.ADMIN) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only recruiter or admin can be company member");
        }

        if (companyMemberRepository.existsByCompanyIdAndUserId(companyId, targetUser.getId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "User is already a member of this company");
        }

        CompanyMember.MemberRole memberRole = parseMemberRole(request.getMemberRole());

        CompanyMember member = new CompanyMember();
        member.setCompany(company);
        member.setUser(targetUser);
        member.setMemberRole(memberRole);
        CompanyMember saved = companyMemberRepository.save(member);
        if (recruiterActivityLogService != null) {
            recruiterActivityLogService.logCompanyMemberAdded(currentUser, company, targetUser, saved.getMemberRole().name());
        }
        return convertToMemberDTO(saved);
    }

    public CompanyMemberDTO updateCompanyMember(Long companyId, Long memberId, CompanyMemberUpsertRequest request) {
        User currentUser = authContextService.requireCurrentUser();
        Company company = companyRepository.findByIdAndDeletedAtIsNull(companyId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Company not found"));

        requireCompanyAdminPermission(currentUser, company);

        CompanyMember member = companyMemberRepository.findByIdAndCompanyId(memberId, companyId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Company member not found"));

        CompanyMember.MemberRole nextRole = parseMemberRole(request.getMemberRole());
        member.setMemberRole(nextRole);
        CompanyMember saved = companyMemberRepository.save(member);
        if (recruiterActivityLogService != null) {
            recruiterActivityLogService.logCompanyMemberUpdated(currentUser, company, saved.getUser(), saved.getMemberRole().name());
        }
        return convertToMemberDTO(saved);
    }

    public void removeCompanyMember(Long companyId, Long memberId) {
        User currentUser = authContextService.requireCurrentUser();
        Company company = companyRepository.findByIdAndDeletedAtIsNull(companyId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Company not found"));

        requireCompanyAdminPermission(currentUser, company);

        CompanyMember member = companyMemberRepository.findByIdAndCompanyId(memberId, companyId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Company member not found"));

        if (company.getCreatedBy() != null && company.getCreatedBy().getId().equals(member.getUser().getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Company owner cannot be removed from members");
        }

        if (member.getMemberRole() == CompanyMember.MemberRole.ADMIN) {
            long adminMembers = companyMemberRepository.countByCompanyIdAndMemberRole(companyId, CompanyMember.MemberRole.ADMIN);
            if (adminMembers <= 1 && company.getCreatedBy() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Company must have at least one admin");
            }
        }

        User removedUser = member.getUser();
        companyMemberRepository.delete(member);
        if (recruiterActivityLogService != null) {
            recruiterActivityLogService.logCompanyMemberRemoved(currentUser, company, removedUser);
        }
    }

    private void requireRecruiterOrAdmin(User user) {
        if (authContextService.isAdmin(user)) {
            return;
        }
        if (user.getRole() != User.Role.RECRUITER) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only recruiter can manage companies");
        }
    }

    private void requireCompanyManagePermission(User user, Company company) {
        if (user.getRole() != User.Role.RECRUITER) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only recruiter can manage companies");
        }
        companyAuthorizationService.requireManageCompany(user, company);
    }

    private void requireCompanyAdminPermission(User user, Company company) {
        if (authContextService.isAdmin(user)) {
            return;
        }
        if (user.getRole() != User.Role.RECRUITER) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only recruiter can manage company members");
        }
        companyAuthorizationService.requireAdministerCompany(user, company);
    }

    private CompanyMember.MemberRole parseMemberRole(String rawRole) {
        if (rawRole == null || rawRole.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "memberRole is required");
        }
        try {
            return CompanyMember.MemberRole.valueOf(rawRole.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid member role");
        }
    }

    private CompanyDTO convertToDTO(Company company) {
        CompanyDTO dto = new CompanyDTO();
        dto.setId(company.getId());
        dto.setName(company.getName());
        dto.setDescription(company.getDescription());
        dto.setLocation(company.getLocation());
        dto.setWebsite(company.getWebsite());
        dto.setIndustry(company.getIndustry());
        dto.setLogoUrl(company.getLogoUrl());
        dto.setJobCount(jobRepository.countByCompanyIdAndStatus(company.getId(), "open"));
        dto.setSavedJobCount(jobRepository.countSavedJobsByCompanyId(company.getId()));
        dto.setFollowerCount(companyFollowRepository.countByCompanyId(company.getId()));
        return dto;
    }

    private JobDTO convertToDTO(JobWithSavedCount projection) {
        JobDTO dto = new JobDTO();
        Job job = projection.getJob();
        dto.setId(job.getId());
        dto.setTitle(job.getTitle());
        dto.setDescription(job.getDescription());
        dto.setLocation(job.getLocation());
        dto.setSalary(job.getSalary());
        dto.setSalaryMin(job.getSalaryMin());
        dto.setSalaryMax(job.getSalaryMax());
        dto.setCurrency(job.getCurrency());
        dto.setJobType(job.getJobType());
        dto.setExperienceLevel(job.getExperienceLevel());
        dto.setCategory(job.getCategory());
        dto.setStatus(job.getStatus());
        dto.setPostedDate(job.getPostedDate());
        dto.setApplicationDeadline(job.getApplicationDeadline());
        if (job.getCompany() != null) {
            dto.setCompanyId(job.getCompany().getId());
            dto.setCompanyName(job.getCompany().getName());
            dto.setCompanyLogoUrl(job.getCompany().getLogoUrl());
        }
        dto.setSavedCount(projection.getSavedCount());
        return dto;
    }

    private CompanyMemberDTO convertToMemberDTO(CompanyMember member) {
        CompanyMemberDTO dto = new CompanyMemberDTO();
        dto.setId(member.getId());
        if (member.getCompany() != null) {
            dto.setCompanyId(member.getCompany().getId());
        }
        if (member.getUser() != null) {
            dto.setUserId(member.getUser().getId());
            dto.setUserName(member.getUser().getName());
            dto.setUserEmail(member.getUser().getEmail());
        }
        if (member.getMemberRole() != null) {
            dto.setMemberRole(member.getMemberRole().name());
        }
        dto.setCreatedAt(member.getCreatedAt());
        return dto;
    }
}
