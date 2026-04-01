package com.ttjobs.backend.config;

import com.ttjobs.backend.security.JwtFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    @Autowired
    private JwtFilter jwtFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
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
                // Keep other routes open for now (you can tighten this later).
                .anyRequest().permitAll()
            )
            // Validate JWT before Spring's username/password auth filter.
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
