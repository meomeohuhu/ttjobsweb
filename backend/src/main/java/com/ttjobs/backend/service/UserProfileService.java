package com.ttjobs.backend.service;

import com.ttjobs.backend.dto.UpdateMyProfileRequest;
import com.ttjobs.backend.dto.ChangePasswordRequest;
import com.ttjobs.backend.dto.UserProfileDTO;
import com.ttjobs.backend.entity.Skill;
import com.ttjobs.backend.entity.User;
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

@Service
public class UserProfileService {

    @Autowired
    private AuthContextService authContextService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SkillRepository skillRepository;

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
}
