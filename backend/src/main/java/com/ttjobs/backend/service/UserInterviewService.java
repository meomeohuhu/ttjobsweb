package com.ttjobs.backend.service;

import com.ttjobs.backend.dto.InterviewScheduleDTO;
import com.ttjobs.backend.entity.InterviewSchedule;
import com.ttjobs.backend.entity.JobApplication;
import com.ttjobs.backend.entity.User;
import com.ttjobs.backend.repository.InterviewScheduleRepository;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserInterviewService {

    @Autowired
    private AuthContextService authContextService;
    @Autowired
    private InterviewScheduleRepository interviewScheduleRepository;

    public List<InterviewScheduleDTO> getMyInterviews() {
        User currentUser = authContextService.requireCurrentUser();
        return interviewScheduleRepository.findByCandidateIdOrderByScheduledAtAsc(currentUser.getId())
                .stream()
                .map(this::toDto)
                .toList();
    }

    private InterviewScheduleDTO toDto(InterviewSchedule interview) {
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
}
