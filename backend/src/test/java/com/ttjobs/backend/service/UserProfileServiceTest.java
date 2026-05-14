package com.ttjobs.backend.service;

import com.ttjobs.backend.dto.user.UpdateMyProfileRequest;
import com.ttjobs.backend.dto.user.UserProfileDTO;
import com.ttjobs.backend.entity.Skill;
import com.ttjobs.backend.entity.User;
import com.ttjobs.backend.repository.SkillRepository;
import com.ttjobs.backend.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserProfileServiceTest {

    @Mock
    private AuthContextService authContextService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private SkillRepository skillRepository;

    @InjectMocks
    private UserProfileService userProfileService;

    @Test
    void getMyProfile_shouldReturnCurrentUser() {
        User user = user(1L);
        user.setName("Thinh");
        when(authContextService.requireCurrentUser()).thenReturn(user);

        UserProfileDTO dto = userProfileService.getMyProfile();
        assertEquals(1L, dto.getId());
        assertEquals("Thinh", dto.getName());
    }

    @Test
    void updateMyProfile_shouldUpdateBasicFieldsAndSkills() {
        User user = user(2L);
        when(authContextService.requireCurrentUser()).thenReturn(user);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(skillRepository.findByNameIgnoreCase("Java")).thenReturn(Optional.empty());
        when(skillRepository.findByNameIgnoreCase("Spring")).thenReturn(Optional.empty());
        when(skillRepository.save(any(Skill.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UpdateMyProfileRequest request = new UpdateMyProfileRequest();
        request.setName("Updated Name");
        request.setPhone("0909");
        request.setAddress("HCM");
        request.setExperienceYears(3);
        request.setSkills(java.util.List.of("Java", "Spring"));

        UserProfileDTO dto = userProfileService.updateMyProfile(request);

        assertEquals("Updated Name", dto.getName());
        assertEquals("0909", dto.getPhone());
        assertEquals("HCM", dto.getAddress());
        assertEquals(3, dto.getExperienceYears());
        assertEquals(2, dto.getSkills().size());
    }

    private User user(Long id) {
        User user = new User();
        user.setId(id);
        user.setEmail("u" + id + "@mail.com");
        user.setRole(User.Role.CANDIDATE);
        return user;
    }
}

