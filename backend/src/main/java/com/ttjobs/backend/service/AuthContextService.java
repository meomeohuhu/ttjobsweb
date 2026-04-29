package com.ttjobs.backend.service;

import com.ttjobs.backend.entity.User;
import com.ttjobs.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@Service
public class AuthContextService {

    @Autowired
    private UserRepository userRepository;

    // Resolve current authenticated user from SecurityContext principal (email).
    public User requireCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
        }

        String email = authentication.getPrincipal().toString();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
    }

    public Optional<User> getCurrentUserOptional() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getPrincipal() == null || !authentication.isAuthenticated()) {
            return Optional.empty();
        }

        String email = authentication.getPrincipal().toString();
        if (email.isBlank() || "anonymousUser".equalsIgnoreCase(email)) {
            return Optional.empty();
        }

        return userRepository.findByEmail(email);
    }

    public boolean isAdmin(User user) {
        return user.getRole() == User.Role.ADMIN;
    }
}
