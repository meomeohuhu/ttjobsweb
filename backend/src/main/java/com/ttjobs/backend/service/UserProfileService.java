package com.ttjobs.backend.service;

import com.ttjobs.backend.dto.user.UpdateMyProfileRequest;
import com.ttjobs.backend.dto.auth.ChangePasswordRequest;
import com.ttjobs.backend.dto.auth.EmailChangeConfirmRequest;
import com.ttjobs.backend.dto.auth.EmailChangeRequest;
import com.ttjobs.backend.dto.auth.EmailChangeResponse;
import com.ttjobs.backend.dto.user.PersonalityProfileDTO;
import com.ttjobs.backend.dto.user.SavePersonalityRequest;
import com.ttjobs.backend.dto.user.UserProfileDTO;
import com.ttjobs.backend.entity.EmailChangeVerification;
import com.ttjobs.backend.entity.Skill;
import com.ttjobs.backend.entity.User;
import com.ttjobs.backend.repository.EmailChangeVerificationRepository;
import com.ttjobs.backend.repository.SkillRepository;
import com.ttjobs.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.time.LocalDateTime;

@Service
public class UserProfileService {

    @Autowired
    private AuthContextService authContextService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SkillRepository skillRepository;
    @Autowired
    private EmailChangeVerificationRepository emailChangeVerificationRepository;
    @Autowired
    private EmailService emailService;
    @Autowired
    private JwtService jwtService;

    public UserProfileDTO getMyProfile() {
        User currentUser = authContextService.requireCurrentUser();
        return toDto(currentUser);
    }

    @Transactional
    public UserProfileDTO updateMyProfile(UpdateMyProfileRequest request) {
        User currentUser = authContextService.requireCurrentUser();

        if (request.getName() != null) {
            currentUser.setName(request.getName().trim());
        }
        if (request.getPhone() != null) {
            currentUser.setPhone(request.getPhone().trim());
        }
        if (request.getAddress() != null) {
            currentUser.setAddress(request.getAddress().trim());
        }
        if (request.getExperienceYears() != null) {
            currentUser.setExperienceYears(request.getExperienceYears());
        }
        if (request.getSkills() != null) {
            currentUser.setSkills(resolveSkills(request.getSkills()));
        }
        if (request.getCvRole() != null) {
            currentUser.setCvRole(request.getCvRole().trim());
        }
        if (request.getCvObjective() != null) {
            currentUser.setCvObjective(request.getCvObjective().trim());
        }
        if (request.getCvExperienceHighlights() != null) {
            currentUser.setCvExperienceHighlights(request.getCvExperienceHighlights().trim());
        }

        return toDto(userRepository.save(currentUser));
    }

    @Transactional
    public void changeMyPassword(ChangePasswordRequest request) {
        User currentUser = authContextService.requireCurrentUser();

        if (request.getNewPassword() == null || request.getConfirmPassword() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password fields are required");
        }
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Passwords do not match");
        }

        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        if (!passwordEncoder.matches(request.getCurrentPassword(), currentUser.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Current password is incorrect");
        }

        currentUser.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(currentUser);
    }

    public PersonalityProfileDTO getMyPersonalityProfile() {
        return toPersonalityDto(authContextService.requireCurrentUser());
    }

    @Transactional
    public void requestEmailChange(EmailChangeRequest request) {
        User currentUser = authContextService.requireCurrentUser();
        String newEmail = normalizeEmail(request.getNewEmail());
        
        if (newEmail.equalsIgnoreCase(currentUser.getEmail())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email mới phải khác với email hiện tại");
        }
        userRepository.findByEmail(newEmail).ifPresent(existing -> {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email này đã được sử dụng bởi một tài khoản khác");
        });

        // Chống spam: 30 giây mới được gửi mã 1 lần
        emailChangeVerificationRepository.findTopByUserIdOrderByCreatedAtDesc(currentUser.getId())
                .ifPresent(lastV -> {
                    if (lastV.getCreatedAt().plusSeconds(30).isAfter(LocalDateTime.now())) {
                        throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Vui lòng đợi 30 giây trước khi yêu cầu mã mới");
                    }
                });

        EmailChangeVerification verification = new EmailChangeVerification();
        verification.setUserId(currentUser.getId());
        verification.setNewEmail(newEmail);
        verification.setCode(generateEmailCode());
        verification.setExpiresAt(LocalDateTime.now().plusMinutes(10));
        emailChangeVerificationRepository.save(verification);
        
        // Gửi mã xác nhận về email HIỆN TẠI của người dùng để đảm bảo chính chủ đang thao tác
        emailService.sendEmailChangeCode(currentUser.getEmail(), verification.getCode());
    }

    @Transactional
    public EmailChangeResponse confirmEmailChange(EmailChangeConfirmRequest request) {
        User currentUser = authContextService.requireCurrentUser();
        String newEmail = normalizeEmail(request.getNewEmail());
        EmailChangeVerification verification = emailChangeVerificationRepository
                .findTopByUserIdAndNewEmailAndUsedAtIsNullOrderByCreatedAtDesc(currentUser.getId(), newEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Mã xác nhận không hợp lệ hoặc đã quá hạn"));

        if (verification.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Mã xác nhận đã hết hạn (10 phút)");
        }
        if (!verification.getCode().equals(request.getCode().trim())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Mã xác nhận không đúng, vui lòng kiểm tra lại");
        }
        userRepository.findByEmail(newEmail).ifPresent(existing -> {
            if (!existing.getId().equals(currentUser.getId())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Email mới đã được sử dụng bởi một tài khoản khác");
            }
        });

        verification.setUsedAt(LocalDateTime.now());
        currentUser.setEmail(newEmail);
        currentUser.setUpdatedAt(LocalDateTime.now());
        User saved = userRepository.save(currentUser);

        EmailChangeResponse response = new EmailChangeResponse();
        response.setEmail(saved.getEmail());
        response.setToken(jwtService.generateToken(saved.getEmail(), saved.getRole().name()));
        return response;
    }

    @Transactional
    public PersonalityProfileDTO saveMyPersonalityProfile(SavePersonalityRequest request) {
        User currentUser = authContextService.requireCurrentUser();
        LocalDateTime now = LocalDateTime.now();

        if (request.getMbtiType() != null) {
            currentUser.setMbtiType(request.getMbtiType().trim().toUpperCase(Locale.ROOT));
            currentUser.setMbtiTakenAt(now);
        }
        if (request.getMiScoresJson() != null) {
            currentUser.setMiScoresJson(request.getMiScoresJson());
            currentUser.setMiTakenAt(now);
        }
        if (request.getPersonalityPublic() != null) {
            currentUser.setPersonalityPublic(request.getPersonalityPublic());
        }
        return toPersonalityDto(userRepository.save(currentUser));
    }

    public PersonalityProfileDTO getPublicPersonalityProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        if (!Boolean.TRUE.equals(user.getPersonalityPublic())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Personality profile is private");
        }
        return toPersonalityDto(user);
    }

    private List<Skill> resolveSkills(List<String> skillNames) {
        Set<String> normalizedNames = new LinkedHashSet<>();
        for (String skillName : skillNames) {
            if (skillName == null) {
                continue;
            }
            String normalized = skillName.trim();
            if (!normalized.isEmpty()) {
                normalizedNames.add(normalized);
            }
        }

        List<Skill> skills = new ArrayList<>();
        for (String skillName : normalizedNames) {
            Skill skill = skillRepository.findByNameIgnoreCase(skillName)
                    .orElseGet(() -> {
                        Skill newSkill = new Skill();
                        newSkill.setName(toTitleCase(skillName));
                        return skillRepository.save(newSkill);
                    });
            skills.add(skill);
        }
        return skills;
    }

    private String toTitleCase(String input) {
        if (input == null || input.isBlank()) {
            return input;
        }
        String[] words = input.trim().toLowerCase(Locale.ROOT).split("\\s+");
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < words.length; i++) {
            String word = words[i];
            if (word.isEmpty()) {
                continue;
            }
            builder.append(Character.toUpperCase(word.charAt(0)));
            if (word.length() > 1) {
                builder.append(word.substring(1));
            }
            if (i < words.length - 1) {
                builder.append(' ');
            }
        }
        return builder.toString();
    }

    private UserProfileDTO toDto(User user) {
        UserProfileDTO dto = new UserProfileDTO();
        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        dto.setRole(user.getRole() != null ? user.getRole().name() : null);
        dto.setPhone(user.getPhone());
        dto.setAddress(user.getAddress());
        dto.setExperienceYears(user.getExperienceYears());
        dto.setCvUrl(user.getCvUrl());
        dto.setPrimaryCvType(user.getPrimaryCvType() != null ? user.getPrimaryCvType().name() : null);
        dto.setCvText(user.getCvText());
        dto.setCvRole(user.getCvRole());
        dto.setCvObjective(user.getCvObjective());
        dto.setCvExperienceHighlights(user.getCvExperienceHighlights());
        dto.setAvatarUrl(user.getAvatarUrl());
        if (user.getSkills() != null) {
            dto.setSkills(user.getSkills().stream().map(Skill::getName).toList());
        } else {
            dto.setSkills(List.of());
        }
        return dto;
    }

    private String normalizeEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email is required");
        }
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private String generateEmailCode() {
        return String.valueOf(ThreadLocalRandom.current().nextInt(100000, 1000000));
    }

    private PersonalityProfileDTO toPersonalityDto(User user) {
        PersonalityProfileDTO dto = new PersonalityProfileDTO();
        dto.setUserId(user.getId());
        dto.setMbtiType(user.getMbtiType());
        dto.setMbtiTakenAt(user.getMbtiTakenAt());
        dto.setMiScoresJson(user.getMiScoresJson());
        dto.setMiTakenAt(user.getMiTakenAt());
        dto.setPersonalityPublic(Boolean.TRUE.equals(user.getPersonalityPublic()));
        return dto;
    }
}

