package com.ttjobs.backend.controller;

import com.ttjobs.backend.dto.AdminRoleUpdateRequest;
import com.ttjobs.backend.dto.AdminStatsDTO;
import com.ttjobs.backend.dto.AdminUserDTO;
import com.ttjobs.backend.dto.CompanyDTO;
import com.ttjobs.backend.dto.JobDTO;
import com.ttjobs.backend.service.AdminService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private AdminService adminService;

    @GetMapping("/users")
    public List<AdminUserDTO> getUsers(@RequestParam(required = false) String role) {
        return adminService.getUsers(role);
    }

    @PutMapping("/users/{id}/role")
    public AdminUserDTO updateUserRole(@PathVariable Long id, @Valid @RequestBody AdminRoleUpdateRequest request) {
        return adminService.updateUserRole(id, request);
    }

    @DeleteMapping("/users/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable Long id) {
        adminService.deleteUser(id);
    }

    @GetMapping("/companies")
    public List<CompanyDTO> getCompanies() {
        return adminService.getCompanies();
    }

    @DeleteMapping("/companies/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCompany(@PathVariable Long id) {
        adminService.deleteCompany(id);
    }

    @GetMapping("/jobs")
    public List<JobDTO> getJobs() {
        return adminService.getJobs();
    }

    @GetMapping("/stats")
    public AdminStatsDTO getStats() {
        return adminService.getStats();
    }
}
