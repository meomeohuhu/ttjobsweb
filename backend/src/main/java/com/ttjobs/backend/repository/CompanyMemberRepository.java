package com.ttjobs.backend.repository;

import com.ttjobs.backend.entity.CompanyMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface CompanyMemberRepository extends JpaRepository<CompanyMember, Long> {
    boolean existsByCompanyIdAndUserId(Long companyId, Long userId);

    boolean existsByCompanyIdAndUserIdAndMemberRoleIn(Long companyId, Long userId,
                                                       Collection<CompanyMember.MemberRole> roles);

    List<CompanyMember> findByCompanyId(Long companyId);

    Optional<CompanyMember> findByIdAndCompanyId(Long id, Long companyId);

    long countByCompanyIdAndMemberRole(Long companyId, CompanyMember.MemberRole memberRole);
}
