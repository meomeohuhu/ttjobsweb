package com.ttjobs.backend.config;

import com.ttjobs.backend.entity.User;
import com.ttjobs.backend.repository.ConversationMemberRepository;
import com.ttjobs.backend.repository.InterviewRoomRepository;
import com.ttjobs.backend.repository.UserRepository;
import com.ttjobs.backend.service.JwtService;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private static final List<String> FRONTEND_DEV_ORIGINS = List.of(
            "http://localhost:5173",
            "http://localhost:5174",
            "http://localhost:5175",
            "http://localhost:5176",
            "http://localhost:5190",
            "http://127.0.0.1:5173",
            "http://127.0.0.1:5174",
            "http://127.0.0.1:5175",
            "http://127.0.0.1:5176",
            "http://127.0.0.1:5190",
            "http://localhost:3000"
    );

    @Autowired
    private JwtService jwtService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ConversationMemberRepository conversationMemberRepository;
    @Autowired
    private InterviewRoomRepository interviewRoomRepository;
    @Value("${ttjobs.cors.allowed-origins:}")
    private String configuredAllowedOrigins;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic");
        registry.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns(allowedOriginPatterns().toArray(String[]::new))
                .withSockJS();
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
                if (accessor == null || accessor.getCommand() == null) {
                    return message;
                }
                if (StompCommand.CONNECT.equals(accessor.getCommand())) {
                    authenticate(accessor);
                }
                if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
                    authorizeConversationSubscription(accessor);
                    authorizeInterviewSubscription(accessor);
                }
                return message;
            }
        });
    }

    private void authenticate(StompHeaderAccessor accessor) {
        String authHeader = accessor.getFirstNativeHeader("Authorization");
        if (authHeader == null || authHeader.isBlank()) {
            return;
        }
        if (!authHeader.startsWith("Bearer ")) {
            throw new AccessDeniedException("Unauthorized websocket connection");
        }

        String token = authHeader.substring(7);
        String email = jwtService.extractEmail(token);
        String role = jwtService.extractRole(token);
        if (email == null || email.isBlank() || role == null || role.isBlank()) {
            throw new AccessDeniedException("Unauthorized websocket connection");
        }

        String authority = "ROLE_" + role.toUpperCase(Locale.ROOT);
        accessor.setUser(new UsernamePasswordAuthenticationToken(
                email,
                null,
                Collections.singletonList(new SimpleGrantedAuthority(authority))
        ));
    }

    private void authorizeConversationSubscription(StompHeaderAccessor accessor) {
        String destination = accessor.getDestination();
        if (destination == null || !destination.startsWith("/topic/conversations/")) {
            return;
        }
        if (accessor.getUser() == null || accessor.getUser().getName() == null) {
            throw new AccessDeniedException("Unauthorized websocket subscription");
        }

        Long conversationId = parseConversationId(destination);
        User user = userRepository.findByEmail(accessor.getUser().getName())
                .orElseThrow(() -> new AccessDeniedException("Unauthorized websocket subscription"));
        if (!conversationMemberRepository.existsByIdConversationIdAndIdUserId(conversationId, user.getId())) {
            throw new AccessDeniedException("Forbidden websocket subscription");
        }
    }

    private void authorizeInterviewSubscription(StompHeaderAccessor accessor) {
        String destination = accessor.getDestination();
        if (destination == null || !destination.startsWith("/topic/interviews/")) {
            return;
        }
        if (accessor.getUser() == null || accessor.getUser().getName() == null) {
            throw new AccessDeniedException("Unauthorized websocket subscription");
        }

        String roomId = parseInterviewRoomId(destination);
        User user = userRepository.findByEmail(accessor.getUser().getName())
                .orElseThrow(() -> new AccessDeniedException("Unauthorized websocket subscription"));
        boolean allowed = interviewRoomRepository.findByRoomIdWithParticipants(roomId)
                .map(room -> room.getInterview() != null
                        && ((room.getInterview().getRecruiter() != null
                            && room.getInterview().getRecruiter().getId().equals(user.getId()))
                        || (room.getInterview().getCandidate() != null
                            && room.getInterview().getCandidate().getId().equals(user.getId()))))
                .orElse(false);
        if (!allowed) {
            throw new AccessDeniedException("Forbidden websocket subscription");
        }
    }

    private Long parseConversationId(String destination) {
        try {
            return Long.parseLong(destination.substring("/topic/conversations/".length()));
        } catch (NumberFormatException ex) {
            throw new AccessDeniedException("Invalid websocket subscription");
        }
    }

    private String parseInterviewRoomId(String destination) {
        String suffix = destination.substring("/topic/interviews/".length());
        int slash = suffix.indexOf('/');
        String roomId = slash >= 0 ? suffix.substring(0, slash) : suffix;
        if (roomId.isBlank()) {
            throw new AccessDeniedException("Invalid websocket subscription");
        }
        return roomId;
    }

    private List<String> allowedOriginPatterns() {
        List<String> origins = new ArrayList<>(FRONTEND_DEV_ORIGINS);
        if (configuredAllowedOrigins != null && !configuredAllowedOrigins.isBlank()) {
            origins.addAll(Arrays.stream(configuredAllowedOrigins.split(","))
                    .map(String::trim)
                    .filter(origin -> !origin.isEmpty())
                    .toList());
        }
        return origins;
    }
}
