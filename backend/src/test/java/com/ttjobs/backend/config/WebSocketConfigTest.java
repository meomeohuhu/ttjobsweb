package com.ttjobs.backend.config;

import com.ttjobs.backend.entity.Conversation;
import com.ttjobs.backend.entity.ConversationMember;
import com.ttjobs.backend.entity.User;
import com.ttjobs.backend.repository.ConversationMemberRepository;
import com.ttjobs.backend.repository.InterviewRoomRepository;
import com.ttjobs.backend.repository.UserRepository;
import com.ttjobs.backend.service.JwtService;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class WebSocketConfigTest {

    private final JwtService jwtService = mock(JwtService.class);
    private final UserRepository userRepository = mock(UserRepository.class);
    private final ConversationMemberRepository conversationMemberRepository = mock(ConversationMemberRepository.class);
    private final InterviewRoomRepository interviewRoomRepository = mock(InterviewRoomRepository.class);
    private final WebSocketConfig config = new WebSocketConfig();

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(config, "jwtService", jwtService);
        ReflectionTestUtils.setField(config, "userRepository", userRepository);
        ReflectionTestUtils.setField(config, "conversationMemberRepository", conversationMemberRepository);
        ReflectionTestUtils.setField(config, "interviewRoomRepository", interviewRoomRepository);
    }

    @Test
    void authenticate_shouldRejectMissingToken() {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.CONNECT);

        assertThrows(AccessDeniedException.class, () -> config.authenticate(accessor));
    }

    @Test
    void authorizeSubscription_shouldRejectOtherUsersTopic() {
        User current = user(1L, User.Role.CANDIDATE);
        StompHeaderAccessor accessor = authenticatedAccessor(current, "/topic/users/2/conversations");

        when(userRepository.findByEmail(current.getEmail())).thenReturn(Optional.of(current));

        assertThrows(AccessDeniedException.class, () -> config.authorizeSubscription(accessor));
    }

    @Test
    void authorizeSubscription_shouldAllowOwnUsersTopic() {
        User current = user(1L, User.Role.CANDIDATE);
        StompHeaderAccessor accessor = authenticatedAccessor(current, "/topic/users/1/conversations");

        when(userRepository.findByEmail(current.getEmail())).thenReturn(Optional.of(current));

        config.authorizeSubscription(accessor);
    }

    @Test
    void authorizeSubscription_shouldAllowConversationMember() {
        User current = user(1L, User.Role.CANDIDATE);
        StompHeaderAccessor accessor = authenticatedAccessor(current, "/topic/conversations/10");

        when(userRepository.findByEmail(current.getEmail())).thenReturn(Optional.of(current));
        when(conversationMemberRepository.existsByIdConversationIdAndIdUserId(10L, 1L)).thenReturn(true);

        config.authorizeSubscription(accessor);
    }

    @Test
    void authorizeSubscription_shouldRejectConversationNonMember() {
        User current = user(1L, User.Role.CANDIDATE);
        StompHeaderAccessor accessor = authenticatedAccessor(current, "/topic/conversations/10");

        when(userRepository.findByEmail(current.getEmail())).thenReturn(Optional.of(current));
        when(conversationMemberRepository.existsByIdConversationIdAndIdUserId(10L, 1L)).thenReturn(false);

        assertThrows(AccessDeniedException.class, () -> config.authorizeSubscription(accessor));
    }

    @Test
    void authorizeSubscription_shouldRejectUnknownTopic() {
        User current = user(1L, User.Role.CANDIDATE);
        StompHeaderAccessor accessor = authenticatedAccessor(current, "/topic/private/anything");

        assertThrows(AccessDeniedException.class, () -> config.authorizeSubscription(accessor));
    }

    private StompHeaderAccessor authenticatedAccessor(User user, String destination) {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.SUBSCRIBE);
        accessor.setDestination(destination);
        accessor.setUser(new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                user.getEmail(),
                null,
                java.util.List.of()));
        return accessor;
    }

    private User user(Long id, User.Role role) {
        User user = new User();
        user.setId(id);
        user.setEmail("u" + id + "@mail.com");
        user.setRole(role);
        return user;
    }
}
