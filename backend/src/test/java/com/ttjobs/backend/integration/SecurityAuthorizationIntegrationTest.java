package com.ttjobs.backend.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ttjobs.backend.entity.Company;
import com.ttjobs.backend.entity.CompanyMember;
import com.ttjobs.backend.entity.User;
import com.ttjobs.backend.repository.CompanyMemberRepository;
import com.ttjobs.backend.repository.CompanyRepository;
import com.ttjobs.backend.repository.UserRepository;
import com.ttjobs.backend.service.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityAuthorizationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private CompanyRepository companyRepository;
    @Autowired
    private CompanyMemberRepository companyMemberRepository;
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void adminEndpoint_shouldReturnForbidden_forCandidateToken() throws Exception {
        User candidate = createUser(User.Role.CANDIDATE);
        String token = bearerToken(candidate);

        mockMvc.perform(get("/api/admin/test")
                        .header("Authorization", token))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminEndpoint_shouldReturnOk_forAdminToken() throws Exception {
        User admin = createUser(User.Role.ADMIN);
        String token = bearerToken(admin);

        mockMvc.perform(get("/api/admin/test")
                        .header("Authorization", token))
                .andExpect(status().isOk());
    }

    @Test
    void adminEndpoint_shouldUseCurrentDatabaseRole_whenTokenHasOldRole() throws Exception {
        User user = createUser(User.Role.CANDIDATE);
        String token = bearerToken(user);
        user.setRole(User.Role.ADMIN);
        userRepository.save(user);

        mockMvc.perform(get("/api/admin/test")
                        .header("Authorization", token))
                .andExpect(status().isOk());
    }

    @Test
    void recruiterMyJobsApplications_shouldReturnForbidden_forCandidateToken() throws Exception {
        User candidate = createUser(User.Role.CANDIDATE);
        String token = bearerToken(candidate);

        mockMvc.perform(get("/api/applications/recruiter/my-jobs")
                        .header("Authorization", token))
                .andExpect(status().isForbidden());
    }

    @Test
    void usersInfo_shouldReturnUnauthorized_whenMissingToken() throws Exception {
        mockMvc.perform(get("/api/users/info"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void usersMe_shouldReturnUnauthorized_whenMissingToken() throws Exception {
        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void usersMeCv_shouldReturnUnauthorized_whenMissingToken() throws Exception {
        mockMvc.perform(get("/api/users/me/cv"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void usersMeAvatar_shouldReturnUnauthorized_whenMissingToken() throws Exception {
        mockMvc.perform(get("/api/users/me/avatar"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void swaggerUi_shouldRequireAuthentication() throws Exception {
        mockMvc.perform(get("/swagger-ui/index.html"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void notifications_shouldReturnUnauthorized_whenMissingToken() throws Exception {
        mockMvc.perform(get("/api/notifications"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void savedJobs_shouldReturnUnauthorized_whenMissingToken() throws Exception {
        mockMvc.perform(get("/api/saved-jobs"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void companyMembers_shouldReturnForbidden_forCandidateToken() throws Exception {
        User owner = createUser(User.Role.RECRUITER);
        Company company = createCompany(owner);
        User candidate = createUser(User.Role.CANDIDATE);

        mockMvc.perform(get("/api/companies/" + company.getId() + "/members")
                        .header("Authorization", bearerToken(candidate)))
                .andExpect(status().isForbidden());
    }

    @Test
    void companyMembers_shouldReturnForbidden_forRecruiterWithoutAdminPermission() throws Exception {
        User owner = createUser(User.Role.RECRUITER);
        Company company = createCompany(owner);
        User recruiter = createUser(User.Role.RECRUITER);

        String body = """
                {"userId": %d, "memberRole": "RECRUITER"}
                """.formatted(owner.getId());

        mockMvc.perform(post("/api/companies/" + company.getId() + "/members")
                        .header("Authorization", bearerToken(recruiter))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isForbidden());
    }

    @Test
    void companyMembers_shouldReturnOk_forOwnerAdmin() throws Exception {
        User owner = createUser(User.Role.RECRUITER);
        Company company = createCompany(owner);
        CompanyMember ownerMember = new CompanyMember();
        ownerMember.setCompany(company);
        ownerMember.setUser(owner);
        ownerMember.setMemberRole(CompanyMember.MemberRole.ADMIN);
        companyMemberRepository.save(ownerMember);

        mockMvc.perform(get("/api/companies/" + company.getId() + "/members")
                        .header("Authorization", bearerToken(owner)))
                .andExpect(status().isOk());
    }

    // Create a test user with unique email to avoid collisions across runs.
    private User createUser(User.Role role) {
        User user = new User();
        user.setName("test-" + role.name().toLowerCase());
        user.setEmail(role.name().toLowerCase() + "-" + System.nanoTime() + "@test.local");
        user.setPasswordHash("dummy-hash");
        user.setRole(role);
        return userRepository.save(user);
    }

    private Company createCompany(User owner) {
        Company company = new Company();
        company.setName("company-" + System.nanoTime());
        company.setCreatedBy(owner);
        return companyRepository.save(company);
    }

    private String bearerToken(User user) {
        String token = jwtService.generateToken(user.getEmail(), user.getRole().name());
        return "Bearer " + token;
    }
}
