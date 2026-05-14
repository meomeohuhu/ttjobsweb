package com.ttjobs.backend.service;

import com.ttjobs.backend.dto.interview.InterviewSignalRequest;
import com.ttjobs.backend.entity.InterviewRoom;
import com.ttjobs.backend.entity.InterviewSchedule;
import com.ttjobs.backend.entity.JobApplication;
import com.ttjobs.backend.entity.User;
import com.ttjobs.backend.repository.InterviewRoomRepository;
import com.ttjobs.backend.repository.InterviewScheduleRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InterviewRoomServiceTest {

    @Mock
    private InterviewScheduleRepository interviewScheduleRepository;
    @Mock
    private InterviewRoomRepository interviewRoomRepository;
    @Mock
    private AuthContextService authContextService;
    @Mock
    private RealtimeEventPublisher realtimeEventPublisher;

    @InjectMocks
    private InterviewRoomService interviewRoomService;

    @Test
    void createRoom_shouldReuseExistingRoom() {
        User recruiter = user(1L, User.Role.RECRUITER);
        InterviewSchedule interview = interview(recruiter, user(2L, User.Role.CANDIDATE));
        InterviewRoom room = room(interview);

        when(authContextService.requireCurrentUser()).thenReturn(recruiter);
        when(interviewScheduleRepository.findById(10L)).thenReturn(Optional.of(interview));
        when(interviewRoomRepository.findByInterviewId(10L)).thenReturn(Optional.of(room));

        assertEquals("room-10", interviewRoomService.createRoom(10L).getRoomId());
    }

    @Test
    void join_shouldSetRoomLiveAndPublishJoinEvent() {
        User candidate = user(2L, User.Role.CANDIDATE);
        InterviewSchedule interview = interview(user(1L, User.Role.RECRUITER), candidate);
        InterviewRoom room = room(interview);

        when(authContextService.requireCurrentUser()).thenReturn(candidate);
        when(interviewScheduleRepository.findById(10L)).thenReturn(Optional.of(interview));
        when(interviewRoomRepository.findByInterviewId(10L)).thenReturn(Optional.of(room));
        when(interviewRoomRepository.save(room)).thenReturn(room);

        var result = interviewRoomService.join(10L);

        assertEquals("LIVE", result.getStatus());
        assertNotNull(room.getStartedAt());
        verify(realtimeEventPublisher).publish(eq("/topic/interviews/room-10/chat"), any());
    }

    @Test
    void join_shouldRejectUnrelatedUser() {
        User unrelated = user(9L, User.Role.CANDIDATE);
        InterviewSchedule interview = interview(user(1L, User.Role.RECRUITER), user(2L, User.Role.CANDIDATE));

        when(authContextService.requireCurrentUser()).thenReturn(unrelated);
        when(interviewScheduleRepository.findById(10L)).thenReturn(Optional.of(interview));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> interviewRoomService.join(10L));
        assertEquals(403, ex.getStatusCode().value());
    }

    @Test
    void end_shouldAllowRecruiterAndSetEnded() {
        User recruiter = user(1L, User.Role.RECRUITER);
        InterviewSchedule interview = interview(recruiter, user(2L, User.Role.CANDIDATE));
        InterviewRoom room = room(interview);

        when(authContextService.requireCurrentUser()).thenReturn(recruiter);
        when(authContextService.isAdmin(recruiter)).thenReturn(false);
        when(interviewScheduleRepository.findById(10L)).thenReturn(Optional.of(interview));
        when(interviewRoomRepository.findByInterviewId(10L)).thenReturn(Optional.of(room));
        when(interviewRoomRepository.save(room)).thenReturn(room);

        var result = interviewRoomService.end(10L);

        assertEquals("ENDED", result.getStatus());
        assertNotNull(result.getEndedAt());
    }

    @Test
    void signal_shouldPublishChatMessageToChatTopic() {
        User recruiter = user(1L, User.Role.RECRUITER);
        InterviewSchedule interview = interview(recruiter, user(2L, User.Role.CANDIDATE));
        InterviewRoom room = room(interview);
        InterviewSignalRequest request = new InterviewSignalRequest();
        request.setType("chat_message");
        request.setPayload("{\"text\":\"hello\"}");

        when(authContextService.requireCurrentUser()).thenReturn(recruiter);
        when(interviewScheduleRepository.findById(10L)).thenReturn(Optional.of(interview));
        when(interviewRoomRepository.findByInterviewId(10L)).thenReturn(Optional.of(room));

        interviewRoomService.signal(10L, request);

        verify(realtimeEventPublisher).publish(eq("/topic/interviews/room-10/chat"), any());
    }

    private InterviewSchedule interview(User recruiter, User candidate) {
        InterviewSchedule interview = new InterviewSchedule();
        interview.setId(10L);
        interview.setRecruiter(recruiter);
        interview.setCandidate(candidate);
        interview.setApplication(new JobApplication());
        return interview;
    }

    private InterviewRoom room(InterviewSchedule interview) {
        InterviewRoom room = new InterviewRoom();
        room.setId(100L);
        room.setInterview(interview);
        room.setRoomId("room-10");
        room.setStatus("WAITING");
        return room;
    }

    private User user(Long id, User.Role role) {
        User user = new User();
        user.setId(id);
        user.setRole(role);
        user.setEmail("u" + id + "@mail.com");
        return user;
    }
}
