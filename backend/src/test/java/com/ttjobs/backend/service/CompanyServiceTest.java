package com.ttjobs.backend.service;

import com.ttjobs.backend.dto.CompanyMemberUpsertRequest;
import com.ttjobs.backend.entity.Company;
import com.ttjobs.backend.entity.CompanyMember;
import com.ttjobs.backend.entity.User;
import com.ttjobs.backend.repository.CompanyMemberRepository;
import com.ttjobs.backend.repository.CompanyRepository;
import com.ttjobs.backend.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CompanyServiceTest {

    @Mock
    private CompanyRepository companyRepository;
    @Mock
    private CompanyMemberRepository companyMemberRepository;
    @Mock
    private AuthContextService authContextService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private CompanyAuthorizationService companyAuthorizationService;

    @InjectMocks
    private CompanyService companyService;

    @Test
    void addCompanyMember_shouldReturnCreatedMember_whenValid() {
        User current = user(1L, User.Role.RECRUITER);
        User recruiter = user(2L, User.Role.RECRUITER);
        Company company = company(10L, current);

        CompanyMemberUpsertRequest req = new CompanyMemberUpsertRequest();
        req.setUserId(2L);
        req.setMemberRole("RECRUITER");

        CompanyMember saved = new CompanyMember();
        saved.setId(99L);
        saved.setCompany(company);
        saved.setUser(recruiter);
        saved.setMemberRole(CompanyMember.MemberRole.RECRUITER);

        when(authContextService.requireCurrentUser()).thenReturn(current);
        when(companyRepository.findByIdAndDeletedAtIsNull(10L)).thenReturn(Optional.of(company));
        doNothing().when(companyAuthorizationService).requireAdministerCompany(current, company);
        when(userRepository.findById(2L)).thenReturn(Optional.of(recruiter));
        when(companyMemberRepository.existsByCompanyIdAndUserId(10L, 2L)).thenReturn(false);
        when(companyMemberRepository.save(org.mockito.ArgumentMatchers.any(CompanyMember.class))).thenReturn(saved);

        assertEquals(99L, companyService.addCompanyMember(10L, req).getId());
    }

    @Test
    void addCompanyMember_shouldReturnConflict_whenDuplicate() {
        User current = user(1L, User.Role.RECRUITER);
        User recruiter = user(2L, User.Role.RECRUITER);
        Company company = company(10L, current);

        CompanyMemberUpsertRequest req = new CompanyMemberUpsertRequest();
        req.setUserId(2L);
        req.setMemberRole("RECRUITER");

        when(authContextService.requireCurrentUser()).thenReturn(current);
        when(companyRepository.findByIdAndDeletedAtIsNull(10L)).thenReturn(Optional.of(company));
        doNothing().when(companyAuthorizationService).requireAdministerCompany(current, company);
        when(userRepository.findById(2L)).thenReturn(Optional.of(recruiter));
        when(companyMemberRepository.existsByCompanyIdAndUserId(10L, 2L)).thenReturn(true);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> companyService.addCompanyMember(10L, req));
        assertEquals(409, ex.getStatusCode().value());
    }

    @Test
    void addCompanyMember_shouldReturnBadRequest_whenTargetIsCandidate() {
        User current = user(1L, User.Role.RECRUITER);
        User candidate = user(2L, User.Role.CANDIDATE);
        Company company = company(10L, current);

        CompanyMemberUpsertRequest req = new CompanyMemberUpsertRequest();
        req.setUserId(2L);
        req.setMemberRole("RECRUITER");

        when(authContextService.requireCurrentUser()).thenReturn(current);
        when(companyRepository.findByIdAndDeletedAtIsNull(10L)).thenReturn(Optional.of(company));
        doNothing().when(companyAuthorizationService).requireAdministerCompany(current, company);
        when(userRepository.findById(2L)).thenReturn(Optional.of(candidate));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> companyService.addCompanyMember(10L, req));
        assertEquals(400, ex.getStatusCode().value());
    }

    @Test
    void addCompanyMember_shouldReturnForbidden_whenRecruiterHasNoAdminPermission() {
        User current = user(1L, User.Role.RECRUITER);
        Company company = company(10L, user(9L, User.Role.RECRUITER));

        CompanyMemberUpsertRequest req = new CompanyMemberUpsertRequest();
        req.setUserId(2L);
        req.setMemberRole("RECRUITER");

        when(authContextService.requireCurrentUser()).thenReturn(current);
        when(companyRepository.findByIdAndDeletedAtIsNull(10L)).thenReturn(Optional.of(company));
        doThrow(new ResponseStatusException(org.springframework.http.HttpStatus.FORBIDDEN, "forbidden"))
                .when(companyAuthorizationService).requireAdministerCompany(current, company);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> companyService.addCompanyMember(10L, req));
        assertEquals(403, ex.getStatusCode().value());
    }

    @Test
    void removeCompanyMember_shouldReturnBadRequest_whenRemovingLastAdminMember() {
        User current = user(1L, User.Role.RECRUITER);
        Company company = company(10L, null);

        CompanyMember member = new CompanyMember();
        member.setId(44L);
        member.setCompany(company);
        member.setUser(user(2L, User.Role.RECRUITER));
        member.setMemberRole(CompanyMember.MemberRole.ADMIN);

        when(authContextService.requireCurrentUser()).thenReturn(current);
        when(companyRepository.findByIdAndDeletedAtIsNull(10L)).thenReturn(Optional.of(company));
        doNothing().when(companyAuthorizationService).requireAdministerCompany(current, company);
        when(companyMemberRepository.findByIdAndCompanyId(44L, 10L)).thenReturn(Optional.of(member));
        when(companyMemberRepository.countByCompanyIdAndMemberRole(10L, CompanyMember.MemberRole.ADMIN)).thenReturn(1L);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> companyService.removeCompanyMember(10L, 44L));
        assertEquals(400, ex.getStatusCode().value());
    }

    @Test
    void removeCompanyMember_shouldReturnBadRequest_whenTargetIsOwner() {
        User current = user(1L, User.Role.RECRUITER);
        User owner = user(10L, User.Role.RECRUITER);
        Company company = company(10L, owner);

        CompanyMember member = new CompanyMember();
        member.setId(44L);
        member.setCompany(company);
        member.setUser(owner);
        member.setMemberRole(CompanyMember.MemberRole.ADMIN);

        when(authContextService.requireCurrentUser()).thenReturn(current);
        when(companyRepository.findByIdAndDeletedAtIsNull(10L)).thenReturn(Optional.of(company));
        doNothing().when(companyAuthorizationService).requireAdministerCompany(current, company);
        when(companyMemberRepository.findByIdAndCompanyId(44L, 10L)).thenReturn(Optional.of(member));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> companyService.removeCompanyMember(10L, 44L));
        assertEquals(400, ex.getStatusCode().value());
    }

    private User user(Long id, User.Role role) {
        User user = new User();
        user.setId(id);
        user.setRole(role);
        user.setEmail("u" + id + "@mail.com");
        return user;
    }

    private Company company(Long id, User owner) {
        Company company = new Company();
        company.setId(id);
        company.setCreatedBy(owner);
        return company;
    }
}
