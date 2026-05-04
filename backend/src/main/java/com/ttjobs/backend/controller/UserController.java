package com.ttjobs.backend.controller;

import com.ttjobs.backend.dto.CandidateDashboardDTO;
import com.ttjobs.backend.dto.InterviewScheduleDTO;
import com.ttjobs.backend.dto.UserCvDTO;
import com.ttjobs.backend.dto.UserCvTextDTO;
import com.ttjobs.backend.dto.UserAvatarDTO;
import com.ttjobs.backend.dto.UserProfileDTO;
import com.ttjobs.backend.dto.EmailChangeConfirmRequest;
import com.ttjobs.backend.dto.EmailChangeRequest;
import com.ttjobs.backend.dto.EmailChangeResponse;
import com.ttjobs.backend.dto.PersonalityProfileDTO;
import com.ttjobs.backend.dto.SavePersonalityRequest;
import com.ttjobs.backend.dto.ChangePasswordRequest;
import com.ttjobs.backend.dto.UpdateMyProfileRequest;
import com.ttjobs.backend.service.CandidateDashboardService;
import com.ttjobs.backend.service.UserAvatarService;
import com.ttjobs.backend.service.UserCvService;
import com.ttjobs.backend.service.UserInterviewService;
import com.ttjobs.backend.service.UserProfileService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api")
public class UserController {

    @Autowired
    private UserCvService userCvService;
    @Autowired
    private UserAvatarService userAvatarService;
    @Autowired
    private UserProfileService userProfileService;
    @Autowired
    private UserInterviewService userInterviewService;
    @Autowired
    private CandidateDashboardService candidateDashboardService;

    @GetMapping("/admin/test")
    public String adminTest() {
        return "Admin access OK";
    }

    @GetMapping("/users/info")
    public String userInfo() {
        return "User Info OK";
    }

    @GetMapping("/users/me")
    public UserProfileDTO getMyProfile() {
        return userProfileService.getMyProfile();
    }

    @GetMapping("/users/me/personality")
    public PersonalityProfileDTO getMyPersonalityProfile() {
        return userProfileService.getMyPersonalityProfile();
    }

    @PostMapping("/users/me/personality")
    public PersonalityProfileDTO saveMyPersonalityProfile(@Valid @RequestBody SavePersonalityRequest request) {
        return userProfileService.saveMyPersonalityProfile(request);
    }

    @GetMapping("/users/{id}/personality")
    public PersonalityProfileDTO getPublicPersonalityProfile(@PathVariable Long id) {
        return userProfileService.getPublicPersonalityProfile(id);
    }

    @GetMapping("/users/me/interviews")
    public List<InterviewScheduleDTO> getMyInterviews() {
        return userInterviewService.getMyInterviews();
    }

    @GetMapping("/users/dashboard")
    public CandidateDashboardDTO getMyDashboard() {
        return candidateDashboardService.getMyDashboard();
    }

    @PutMapping("/users/me")
    public UserProfileDTO updateMyProfile(@Valid @RequestBody UpdateMyProfileRequest request) {
        return userProfileService.updateMyProfile(request);
    }

    @PostMapping("/users/me/email-change/request")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void requestEmailChange(@Valid @RequestBody EmailChangeRequest request) {
        userProfileService.requestEmailChange(request);
    }

    @PostMapping("/users/me/email-change/confirm")
    public EmailChangeResponse confirmEmailChange(@Valid @RequestBody EmailChangeConfirmRequest request) {
        return userProfileService.confirmEmailChange(request);
    }

    @PutMapping("/users/me/password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changeMyPassword(@Valid @RequestBody ChangePasswordRequest request) {
        userProfileService.changeMyPassword(request);
    }

    @GetMapping("/users/me/cv")
    public UserCvDTO getMyCv() {
        return userCvService.getMyCv();
    }

    @GetMapping("/users/me/cvs")
    public List<UserCvDTO> getMyCvs() {
        return userCvService.getMyCvs();
    }

    @PostMapping(value = "/users/me/cv", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public UserCvDTO uploadMyCv(@RequestPart("file") MultipartFile file) {
        return userCvService.uploadMyCv(file);
    }

    @PutMapping(value = "/users/me/cv", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public UserCvDTO replaceMyCv(@RequestPart("file") MultipartFile file) {
        return userCvService.uploadMyCv(file);
    }

    @DeleteMapping("/users/me/cv")
    public void deleteMyCv() {
        userCvService.deleteMyCv();
    }

    @DeleteMapping("/users/me/cvs/{id}")
    public void deleteMyCvById(@PathVariable Long id) {
        userCvService.deleteMyCvById(id);
    }

    @GetMapping("/users/me/cv-stream")
    public void streamMyCurrentCv(HttpServletResponse response) {
        userCvService.streamMyCurrentCv(response);
    }

    @GetMapping("/users/me/cvs/{id}/stream")
    public void streamMyCvById(@PathVariable Long id, HttpServletResponse response) {
        userCvService.streamMyCvById(id, response);
    }

    @GetMapping("/users/me/cv-text")
    public UserCvTextDTO getMyCvText() {
        return userCvService.getMyCvText();
    }

    @PostMapping("/users/me/cv-text")
    public UserCvTextDTO extractMyCvText() {
        return userCvService.extractMyCvText();
    }

    @PostMapping("/users/me/cv/parse-skills")
    public List<String> parseMyCvSkills() {
        return userCvService.parseMyCvSkills();
    }

    @GetMapping("/users/me/avatar")
    public UserAvatarDTO getMyAvatar() {
        return userAvatarService.getMyAvatar();
    }

    @PostMapping(value = "/users/me/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public UserAvatarDTO uploadMyAvatar(@RequestPart("file") MultipartFile file) {
        return userAvatarService.uploadMyAvatar(file);
    }

    @PutMapping(value = "/users/me/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public UserAvatarDTO replaceMyAvatar(@RequestPart("file") MultipartFile file) {
        return userAvatarService.uploadMyAvatar(file);
    }

    @DeleteMapping("/users/me/avatar")
    public void deleteMyAvatar() {
        userAvatarService.deleteMyAvatar();
    }
}
