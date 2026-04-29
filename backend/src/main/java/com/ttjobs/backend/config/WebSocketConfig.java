package com.ttjobs.backend.config;

import com.ttjobs.backend.entity.User;
import com.ttjobs.backend.repository.ConversationMemberRepository;
import com.ttjobs.backend.repository.UserRepository;
import com.ttjobs.backend.service.JwtService;
import java.util.Collections;
import java.util.Locale;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    private JwtService jwtService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ConversationMemberRepository conversationMemberRepository;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic");
        registry.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns(
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
                }
                return message;
            }
        });
    }

    private void authenticate(StompHeaderAccessor accessor) {
        String authHeader = accessor.getFirstNativeHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
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

    private Long parseConversationId(String destination) {
        try {
            return Long.parseLong(destination.substring("/topic/conversations/".length()));
        } catch (NumberFormatException ex) {
            throw new AccessDeniedException("Invalid websocket subscription");
        }
    }
}
