package com.ttjobs.backend.controller;

import com.ttjobs.backend.dto.user.CandidateDashboardDTO;
import com.ttjobs.backend.dto.user.UserCvDTO;
import com.ttjobs.backend.dto.user.UserAvatarDTO;
import com.ttjobs.backend.service.CandidateDashboardService;
import com.ttjobs.backend.service.JwtService;
import com.ttjobs.backend.service.UserAvatarService;
import com.ttjobs.backend.service.UserCvService;
import com.ttjobs.backend.service.UserInterviewService;
import com.ttjobs.backend.service.UserProfileService;
import com.ttjobs.backend.dto.user.UserProfileDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerApiTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserCvService userCvService;
    @MockBean
    private UserAvatarService userAvatarService;
    @MockBean
    private UserProfileService userProfileService;
    @MockBean
    private UserInterviewService userInterviewService;
    @MockBean
    private CandidateDashboardService candidateDashboardService;
    @MockBean
    private JwtService jwtService;

    @Test
    void getMyProfile_shouldReturnProfile() throws Exception {
        UserProfileDTO dto = new UserProfileDTO();
        dto.setId(1L);
        dto.setName("Thinh");
        dto.setEmail("test@gmail.com");
        when(userProfileService.getMyProfile()).thenReturn(dto);

        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Thinh"));
    }

    @Test
    void updateMyProfile_shouldReturnUpdatedProfile() throws Exception {
        UserProfileDTO dto = new UserProfileDTO();
        dto.setId(1L);
        dto.setName("Thinh Updated");
        when(userProfileService.updateMyProfile(any())).thenReturn(dto);

        String body = """
                {
                  "name": "Thinh Updated",
                  "phone": "0123"
                }
                """;

        mockMvc.perform(put("/api/users/me")
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Thinh Updated"));
    }

    @Test
    void getMyDashboard_shouldReturnCandidateDashboard() throws Exception {
        CandidateDashboardDTO dto = new CandidateDashboardDTO();
        dto.setAppliedCount(2);
        dto.setSavedCount(3);
        dto.setUpcomingInterviewCount(1);
        dto.setUnreadMessageCount(4);
        dto.setProfileCompletionPercent(75);
        dto.setMissingProfileItems(java.util.List.of("CV", "Kỹ năng"));
        dto.setRecentApplications(java.util.List.of());
        dto.setRecommendedJobs(java.util.List.of());
        dto.setUpcomingInterviews(java.util.List.of());
        when(candidateDashboardService.getMyDashboard()).thenReturn(dto);

        mockMvc.perform(get("/api/users/dashboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.appliedCount").value(2))
                .andExpect(jsonPath("$.savedCount").value(3))
                .andExpect(jsonPath("$.upcomingInterviewCount").value(1))
                .andExpect(jsonPath("$.unreadMessageCount").value(4))
                .andExpect(jsonPath("$.profileCompletionPercent").value(75))
                .andExpect(jsonPath("$.missingProfileItems[0]").value("CV"));
    }

    @Test
    void getMyCv_shouldReturnCvDto() throws Exception {
        UserCvDTO dto = new UserCvDTO();
        dto.setUserId(1L);
        dto.setCvUrl("https://res.cloudinary.com/demo/raw/upload/v1/ttjobs/cv/cv.pdf");
        when(userCvService.getMyCv()).thenReturn(dto);

        mockMvc.perform(get("/api/users/me/cv"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.cvUrl").value("https://res.cloudinary.com/demo/raw/upload/v1/ttjobs/cv/cv.pdf"));
    }

    @Test
    void uploadMyCv_shouldReturnCvDto() throws Exception {
        UserCvDTO dto = new UserCvDTO();
        dto.setUserId(2L);
        dto.setCvUrl("https://res.cloudinary.com/demo/raw/upload/v1/ttjobs/cv/newcv.pdf");
        when(userCvService.uploadMyCv(any())).thenReturn(dto);

        MockMultipartFile file = new MockMultipartFile(
                "file", "cv.pdf", "application/pdf", "hello".getBytes()
        );

        mockMvc.perform(multipart("/api/users/me/cv").file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(2));
    }

    @Test
    void deleteMyCv_shouldReturnOk() throws Exception {
        doNothing().when(userCvService).deleteMyCv();

        mockMvc.perform(delete("/api/users/me/cv"))
                .andExpect(status().isOk());
    }

    @Test
    void getMyAvatar_shouldReturnAvatarDto() throws Exception {
        UserAvatarDTO dto = new UserAvatarDTO();
        dto.setUserId(1L);
        dto.setAvatarUrl("https://res.cloudinary.com/demo/image/upload/v1/ttjobs/avatar/avatar.png");
        when(userAvatarService.getMyAvatar()).thenReturn(dto);

        mockMvc.perform(get("/api/users/me/avatar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.avatarUrl").value("https://res.cloudinary.com/demo/image/upload/v1/ttjobs/avatar/avatar.png"));
    }

    @Test
    void uploadMyAvatar_shouldReturnAvatarDto() throws Exception {
        UserAvatarDTO dto = new UserAvatarDTO();
        dto.setUserId(2L);
        dto.setAvatarUrl("https://res.cloudinary.com/demo/image/upload/v1/ttjobs/avatar/newavatar.png");
        when(userAvatarService.uploadMyAvatar(any())).thenReturn(dto);

        MockMultipartFile file = new MockMultipartFile(
                "file", "avatar.png", "image/png", "hello".getBytes()
        );

        mockMvc.perform(multipart("/api/users/me/avatar").file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(2));
    }

    @Test
    void deleteMyAvatar_shouldReturnOk() throws Exception {
        doNothing().when(userAvatarService).deleteMyAvatar();

        mockMvc.perform(delete("/api/users/me/avatar"))
                .andExpect(status().isOk());
    }
}

