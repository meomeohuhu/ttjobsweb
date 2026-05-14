package com.ttjobs.backend.service;

import com.ttjobs.backend.dto.interview.InterviewRoomDTO;
import com.ttjobs.backend.dto.interview.InterviewSignalRequest;
import com.ttjobs.backend.entity.InterviewRoom;
import com.ttjobs.backend.entity.InterviewSchedule;
import com.ttjobs.backend.entity.User;
import com.ttjobs.backend.repository.InterviewRoomRepository;
import com.ttjobs.backend.repository.InterviewScheduleRepository;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class InterviewRoomService {

    @Autowired
    private InterviewScheduleRepository interviewScheduleRepository;
    @Autowired
    private InterviewRoomRepository interviewRoomRepository;
    @Autowired
    private AuthContextService authContextService;
    @Autowired
    private RealtimeEventPublisher realtimeEventPublisher;

    @Transactional
    public synchronized InterviewRoomDTO createRoom(Long interviewId) {
        User currentUser = authContextService.requireCurrentUser();
        InterviewSchedule interview = requireInterview(interviewId);
        requireParticipant(currentUser, interview);
        InterviewRoom room = interviewRoomRepository.findByInterviewId(interviewId).orElse(null);
        if (room != null) {
            return toDto(room);
        }

        InterviewRoom created = new InterviewRoom();
        created.setInterview(interview);
        created.setRoomId("interview-" + interviewId + "-" + UUID.randomUUID());
        created.setStatus("WAITING");
        return toDto(interviewRoomRepository.save(created));
    }

    @Transactional
    public InterviewRoomDTO join(Long interviewId) {
        User currentUser = authContextService.requireCurrentUser();
        InterviewSchedule interview = requireInterview(interviewId);
        requireParticipant(currentUser, interview);
        InterviewRoom room = interviewRoomRepository.findByInterviewId(interviewId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Interview room not found"));
        if (room.getStartedAt() == null) {
            room.setStartedAt(LocalDateTime.now());
        }
        room.setStatus("LIVE");
        InterviewRoom saved = interviewRoomRepository.save(room);
        publish(saved, "participant_joined", currentUser.getId());
        return toDto(saved);
    }

    @Transactional
    public InterviewRoomDTO leave(Long interviewId) {
        User currentUser = authContextService.requireCurrentUser();
        InterviewSchedule interview = requireInterview(interviewId);
        requireParticipant(currentUser, interview);
        InterviewRoom room = interviewRoomRepository.findByInterviewId(interviewId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Interview room not found"));
        publish(room, "participant_left", currentUser.getId());
        return toDto(room);
    }

    @Transactional
    public InterviewRoomDTO end(Long interviewId) {
        User currentUser = authContextService.requireCurrentUser();
        InterviewSchedule interview = requireInterview(interviewId);
        requireRecruiterOrAdmin(currentUser, interview);
        InterviewRoom room = interviewRoomRepository.findByInterviewId(interviewId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Interview room not found"));
        room.setStatus("ENDED");
        room.setEndedAt(LocalDateTime.now());
        InterviewRoom saved = interviewRoomRepository.save(room);
        publish(saved, "room_ended", currentUser.getId());
        return toDto(saved);
    }

    public void signal(Long interviewId, InterviewSignalRequest request) {
        User currentUser = authContextService.requireCurrentUser();
        InterviewSchedule interview = requireInterview(interviewId);
        requireParticipant(currentUser, interview);
        InterviewRoom room = interviewRoomRepository.findByInterviewId(interviewId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Interview room not found"));
        String type = request == null || request.getType() == null || request.getType().isBlank()
                ? "signal"
                : request.getType();
        String payload = request == null || request.getPayload() == null ? "" : request.getPayload();
        String destination = "chat_message".equals(type)
                ? "/topic/interviews/" + room.getRoomId() + "/chat"
                : "/topic/interviews/" + room.getRoomId() + "/signal";
        realtimeEventPublisher.publish(destination, Map.of(
                "type", type,
                "payload", payload,
                "actorId", currentUser.getId(),
                "roomId", room.getRoomId()
        ));
    }

    private InterviewSchedule requireInterview(Long interviewId) {
        return interviewScheduleRepository.findById(interviewId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Interview not found"));
    }

    private void requireParticipant(User user, InterviewSchedule interview) {
        boolean recruiter = interview.getRecruiter() != null && interview.getRecruiter().getId().equals(user.getId());
        boolean candidate = interview.getCandidate() != null && interview.getCandidate().getId().equals(user.getId());
        if (!recruiter && !candidate) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You cannot access this interview");
        }
    }

    private void requireRecruiterOrAdmin(User user, InterviewSchedule interview) {
        if (authContextService.isAdmin(user)) return;
        boolean recruiter = interview.getRecruiter() != null && interview.getRecruiter().getId().equals(user.getId());
        if (!recruiter) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only recruiter can end this interview room");
        }
    }

    private void publish(InterviewRoom room, String type, Long actorId) {
        realtimeEventPublisher.publish("/topic/interviews/" + room.getRoomId() + "/chat", Map.of(
                "type", type,
                "actorId", actorId,
                "roomId", room.getRoomId()
        ));
    }

    private InterviewRoomDTO toDto(InterviewRoom room) {
        InterviewRoomDTO dto = new InterviewRoomDTO();
        dto.setId(room.getId());
        dto.setInterviewId(room.getInterview() == null ? null : room.getInterview().getId());
        dto.setRoomId(room.getRoomId());
        dto.setStatus(room.getStatus());
        dto.setStartedAt(room.getStartedAt());
        dto.setEndedAt(room.getEndedAt());
        return dto;
    }
}
