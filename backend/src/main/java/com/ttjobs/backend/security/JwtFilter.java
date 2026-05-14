package com.ttjobs.backend.security;

import com.ttjobs.backend.entity.User;
import com.ttjobs.backend.repository.UserRepository;
import com.ttjobs.backend.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.Locale;

@Component
public class JwtFilter extends OncePerRequestFilter {

    @Autowired
    private JwtService jwtService;
    @Autowired
    private ObjectProvider<UserRepository> userRepositoryProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        // No bearer token: continue without authentication.
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Extract token from header.
        String token = authHeader.substring(7);

        try {
            String email = jwtService.extractEmail(token);
            String role = jwtService.extractRole(token);
            UserRepository userRepository = userRepositoryProvider.getIfAvailable();
            String currentRole = userRepository == null
                    ? role
                    : userRepository.findByEmail(email)
                            .map(User::getRole)
                            .map(Enum::name)
                            .orElse(role);

            if (currentRole == null || currentRole.isBlank()) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }

            // Luon uu tien role moi nhat trong DB de admin doi quyen co hieu luc ngay,
            // tranh token cu van giu role cu va gay 403 sau khi nang quyen.
            String authority = "ROLE_" + currentRole.toUpperCase(Locale.ROOT);
            UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                email,
                null,
                Collections.singletonList(new SimpleGrantedAuthority(authority))
            );

            SecurityContextHolder.getContext().setAuthentication(auth);
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        String method = request.getMethod();
        if (path == null) {
            return false;
        }
        if (path.startsWith("/api/auth/")) {
            return true;
        }
        if (path.startsWith("/ws")) {
            return true;
        }
        if (path.startsWith("/swagger-ui/") || path.startsWith("/v3/api-docs/")
                || path.equals("/openapi.yaml") || path.equals("/actuator/health")) {
            return true;
        }
        if ("GET".equalsIgnoreCase(method) && path.startsWith("/api/jobs")) {
            return true;
        }
        return false;
    }
}
