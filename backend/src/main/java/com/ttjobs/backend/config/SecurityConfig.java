package com.ttjobs.backend.config;

import com.ttjobs.backend.security.JwtFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import jakarta.servlet.http.HttpServletResponse;

@Configuration
public class SecurityConfig {

    @Autowired
    private JwtFilter jwtFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        AuthenticationEntryPoint unauthorizedEntryPoint = (request, response, authException) ->
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");

        http
            // Disable CSRF for stateless JWT APIs.
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                // Public endpoints: registration and login.
                .requestMatchers("/api/auth/**").permitAll()
                // Only users with ROLE_ADMIN can access admin routes.
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                // Any authenticated user can access user profile routes.
                .requestMatchers("/api/users/**").authenticated()
                // Company, jobs, applications, saved jobs and notifications are protected APIs.
                .requestMatchers("/api/companies/**", "/api/jobs/**", "/api/applications/**", "/api/saved-jobs/**", "/api/notifications/**").authenticated()
                // Keep other routes open for now (you can tighten this later).
                .anyRequest().permitAll()
            )
            // Return 401 for unauthenticated requests; keep 403 for access denied.
            .exceptionHandling(ex -> ex.authenticationEntryPoint(unauthorizedEntryPoint))
            // Validate JWT before Spring's username/password auth filter.
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
