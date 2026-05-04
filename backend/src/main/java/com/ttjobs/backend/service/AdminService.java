package com.ttjobs.backend.service;

import com.ttjobs.backend.dto.AdminRoleUpdateRequest;
import com.ttjobs.backend.dto.AdminStatsDTO;
import com.ttjobs.backend.dto.AdminUserDTO;
import com.ttjobs.backend.dto.CompanyDTO;
import com.ttjobs.backend.dto.JobDTO;
import com.ttjobs.backend.entity.Company;
import com.ttjobs.backend.entity.Job;
import com.ttjobs.backend.entity.User;
import com.ttjobs.backend.repository.CompanyFollowRepository;
import com.ttjobs.backend.repository.CompanyRepository;
import com.ttjobs.backend.repository.JobApplicationRepository;
import com.ttjobs.backend.repository.JobRepository;
import com.ttjobs.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AdminService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private CompanyRepository companyRepository;
    @Autowired
    private JobRepository jobRepository;
    @Autowired
    private JobApplicationRepository jobApplicationRepository;
    @Autowired
    private CompanyFollowRepository companyFollowRepository;

    public List<AdminUserDTO> getUsers(String role) {
        return userRepository.findAll().stream()
                .filter(user -> role == null || role.isBlank() || user.getRole().name().equalsIgnoreCase(role.trim()))
                .map(this::toAdminUserDto)
                .toList();
    }

    @Transactional
    public AdminUserDTO updateUserRole(Long id, AdminRoleUpdateRequest request) {
        User user = userRepository.findById(id).orElseThrow();
        user.setRole(User.Role.valueOf(request.getRole().trim().toUpperCase()));
        return toAdminUserDto(userRepository.save(user));
    }

    @Transactional
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    public List<CompanyDTO> getCompanies() {
        return companyRepository.findAll().stream().map(this::toCompanyDto).toList();
    }

    @Transactional
    public void deleteCompany(Long id) {
        Company company = companyRepository.findById(id).orElseThrow();
        company.setDeletedAt(LocalDateTime.now());
        companyRepository.save(company);
    }

    public List<JobDTO> getJobs() {
        return jobRepository.findAll().stream().map(this::toJobDto).toList();
    }

    public AdminStatsDTO getStats() {
        List<User> users = userRepository.findAll();
        AdminStatsDTO dto = new AdminStatsDTO();
        dto.setTotalUsers(users.size());
        dto.setTotalCandidates(users.stream().filter(user -> user.getRole() == User.Role.CANDIDATE).count());
        dto.setTotalRecruiters(users.stream().filter(user -> user.getRole() == User.Role.RECRUITER).count());
        dto.setTotalAdmins(users.stream().filter(user -> user.getRole() == User.Role.ADMIN).count());
        dto.setTotalCompanies(companyRepository.count());
        dto.setTotalJobs(jobRepository.count());
        dto.setTotalApplications(jobApplicationRepository.count());
        return dto;
    }

    private AdminUserDTO toAdminUserDto(User user) {
        AdminUserDTO dto = new AdminUserDTO();
        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        dto.setRole(user.getRole() != null ? user.getRole().name() : null);
        dto.setPhone(user.getPhone());
        dto.setCreatedAt(user.getCreatedAt());
        return dto;
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
        dto.setJobCount(company.getId() == null ? 0L : jobRepository.countByCompanyIdAndStatus(company.getId(), "open"));
        dto.setSavedJobCount(company.getId() == null ? 0L : jobRepository.countSavedJobsByCompanyId(company.getId()));
        dto.setFollowerCount(company.getId() == null ? 0L : companyFollowRepository.countByCompanyId(company.getId()));
        return dto;
    }

    private JobDTO toJobDto(Job job) {
        JobDTO dto = new JobDTO();
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
        dto.setImageUrl(job.getImageUrl());
        dto.setStatus(job.getStatus());
        dto.setPostedDate(job.getPostedDate());
        dto.setApplicationDeadline(job.getApplicationDeadline());
        if (job.getCompany() != null) {
            dto.setCompanyId(job.getCompany().getId());
            dto.setCompanyName(job.getCompany().getName());
            dto.setCompanyLogoUrl(job.getCompany().getLogoUrl());
        }
        return dto;
    }
}
