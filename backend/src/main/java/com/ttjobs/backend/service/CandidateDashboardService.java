package com.ttjobs.backend.service;

import com.ttjobs.backend.dto.CandidateDashboardDTO;
import com.ttjobs.backend.dto.InterviewScheduleDTO;
import com.ttjobs.backend.dto.JobApplicationDTO;
import com.ttjobs.backend.dto.JobDTO;
import com.ttjobs.backend.entity.ConversationMember;
import com.ttjobs.backend.entity.InterviewSchedule;
import com.ttjobs.backend.entity.JobApplication;
import com.ttjobs.backend.entity.JobNeedPreference;
import com.ttjobs.backend.entity.Skill;
import com.ttjobs.backend.entity.User;
import com.ttjobs.backend.repository.ConversationMemberRepository;
import com.ttjobs.backend.repository.InterviewScheduleRepository;
import com.ttjobs.backend.repository.JobApplicationRepository;
import com.ttjobs.backend.repository.MessageRepository;
import com.ttjobs.backend.repository.SavedJobRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class CandidateDashboardService {

    @Autowired
    private AuthContextService authContextService;
    @Autowired
    private JobApplicationRepository jobApplicationRepository;
    @Autowired
    private SavedJobRepository savedJobRepository;
    @Autowired
    private InterviewScheduleRepository interviewScheduleRepository;
    @Autowired
    private ConversationMemberRepository conversationMemberRepository;
    @Autowired
    private MessageRepository messageRepository;
    @Autowired
    private JobNeedPreferenceService jobNeedPreferenceService;
    @Autowired
    private RecommendationService recommendationService;

    public CandidateDashboardDTO getMyDashboard() {
        User currentUser = authContextService.requireCurrentUser();
        if (currentUser.getRole() != User.Role.CANDIDATE) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only candidate can access dashboard");
        }

        List<JobApplication> applications = jobApplicationRepository.findByUserId(currentUser.getId());
        List<InterviewSchedule> interviews = interviewScheduleRepository.findByCandidateIdOrderByScheduledAtAsc(currentUser.getId());
        JobNeedPreference preference = jobNeedPreferenceService.getOrCreate(currentUser.getId());

        CandidateDashboardDTO dto = new CandidateDashboardDTO();
        dto.setAppliedCount(applications.size());
        dto.setSavedCount(savedJobRepository.findByUserIdOrderBySavedAtDesc(currentUser.getId()).size());
        dto.setUnreadMessageCount(countUnreadMessages(currentUser));
        dto.setMissingProfileItems(missingProfileItems(currentUser, preference));
        dto.setProfileCompletionPercent(profileCompletionPercent(dto.getMissingProfileItems().size()));
        dto.setRecentApplications(recentApplications(applications));
        dto.setUpcomingInterviews(upcomingInterviews(interviews));
        dto.setUpcomingInterviewCount(dto.getUpcomingInterviews().size());
        dto.setRecommendedJobs(recommendedJobs());
        return dto;
    }

    private long countUnreadMessages(User user) {
        long total = 0;
        LocalDateTime fallback = LocalDateTime.of(1970, 1, 1, 0, 0);
        for (ConversationMember member : conversationMemberRepository.findByIdUserId(user.getId())) {
            if (member.getConversation() == null || member.getConversation().getId() == null) {
                continue;
            }
            LocalDateTime after = member.getLastReadAt() == null ? fallback : member.getLastReadAt();
            total += messageRepository.countByConversationIdAndSenderIdNotAndCreatedAtAfter(
                    member.getConversation().getId(),
                    user.getId(),
                    after
            );
        }
        return total;
    }

    private List<String> missingProfileItems(User user, JobNeedPreference preference) {
        List<String> missing = new ArrayList<>();
        if (!hasText(user.getName())) missing.add("Họ và tên");
        if (!hasText(user.getPhone())) missing.add("Số điện thoại");
        if (!hasText(user.getAddress())) missing.add("Địa chỉ");
        if (!hasText(user.getAvatarUrl())) missing.add("Ảnh đại diện");
        if (!hasText(user.getCvUrl())) missing.add("CV");
        if (user.getSkills() == null || user.getSkills().isEmpty()) missing.add("Kỹ năng");
        if (user.getExperienceYears() == null) missing.add("Kinh nghiệm");
        if (!jobNeedPreferenceService.hasConfiguredCriteria(preference)) missing.add("Nhu cầu tìm việc");
        return missing;
    }

    private int profileCompletionPercent(int missingCount) {
        int total = 8;
        int completed = Math.max(0, total - missingCount);
        return Math.round((completed * 100f) / total);
    }

    private List<JobApplicationDTO> recentApplications(List<JobApplication> applications) {
        return applications.stream()
                .sorted(Comparator.comparing(JobApplication::getApplicationDate, Comparator.nullsLast(Comparator.naturalOrder())).reversed())
                .limit(5)
                .map(this::toApplicationDto)
                .toList();
    }

    private List<InterviewScheduleDTO> upcomingInterviews(List<InterviewSchedule> interviews) {
        LocalDateTime now = LocalDateTime.now();
        return interviews.stream()
                .filter(item -> item.getScheduledAt() != null && !item.getScheduledAt().isBefore(now))
                .filter(item -> {
                    String status = item.getStatus() == null ? "" : item.getStatus().toLowerCase();
                    return !status.equals("cancelled") && !status.equals("completed");
                })
                .limit(5)
                .map(this::toInterviewDto)
                .toList();
    }

    private List<JobDTO> recommendedJobs() {
        try {
            return recommendationService.recommendByJobNeeds().stream().limit(6).toList();
        } catch (ResponseStatusException ex) {
            return List.of();
        }
    }

    private JobApplicationDTO toApplicationDto(JobApplication application) {
        JobApplicationDTO dto = new JobApplicationDTO();
        dto.setId(application.getId());
        dto.setApplicationDate(application.getApplicationDate());
        dto.setStatus(application.getStatus());
        if (application.getUser() != null) {
            dto.setUserId(application.getUser().getId());
            dto.setUserName(application.getUser().getName());
        }
        if (application.getJob() != null) {
            dto.setJobId(application.getJob().getId());
            dto.setJobTitle(application.getJob().getTitle());
            if (application.getJob().getCompany() != null) {
                dto.setCompanyName(application.getJob().getCompany().getName());
            }
        }
        dto.setHasCv(hasText(application.getCvUrl()));
        return dto;
    }

    private InterviewScheduleDTO toInterviewDto(InterviewSchedule interview) {
        InterviewScheduleDTO dto = new InterviewScheduleDTO();
        dto.setId(interview.getId());
        dto.setScheduledAt(interview.getScheduledAt());
        dto.setDurationMinutes(interview.getDurationMinutes());
        dto.setLocation(interview.getLocation());
        dto.setMeetingLink(interview.getMeetingLink());
        dto.setNote(interview.getNote());
        dto.setStatus(interview.getStatus());
        dto.setCreatedAt(interview.getCreatedAt());
        JobApplication application = interview.getApplication();
        if (application != null) {
            dto.setApplicationId(application.getId());
            if (application.getUser() != null) {
                dto.setCandidateId(application.getUser().getId());
                dto.setCandidateName(application.getUser().getName());
            }
            if (application.getJob() != null) {
                dto.setJobId(application.getJob().getId());
                dto.setJobTitle(application.getJob().getTitle());
                if (application.getJob().getCompany() != null) {
                    dto.setCompanyId(application.getJob().getCompany().getId());
                    dto.setCompanyName(application.getJob().getCompany().getName());
                }
            }
        }
        return dto;
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
