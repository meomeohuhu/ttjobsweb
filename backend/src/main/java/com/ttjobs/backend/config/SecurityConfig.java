package com.ttjobs.backend.config;

import com.ttjobs.backend.security.JwtFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.http.HttpMethod;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import jakarta.servlet.http.HttpServletResponse;
import java.util.List;

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
            // Enable CORS for frontend dev server.
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .authorizeHttpRequests(auth -> auth
                // Public endpoints: registration and login.
                .requestMatchers("/api/auth/**").permitAll()
                // Swagger UI and static OpenAPI spec.
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/openapi.yaml", "/actuator/health").permitAll()
                // Public job listings and job detail.
                .requestMatchers(HttpMethod.GET, "/api/jobs/**").permitAll()
                // Public company pages.
                .requestMatchers(HttpMethod.GET, "/api/companies/**").permitAll()
                // Authenticated company follow actions.
                .requestMatchers("/api/company-follows/**").authenticated()
                // Recruiter dashboard and future recruiter workspace routes.
                .requestMatchers("/api/recruiter/**").authenticated()
                // Only users with ROLE_ADMIN can access admin routes.
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                // Any authenticated user can access user profile routes.
                .requestMatchers("/api/users/**").authenticated()
                // Company, jobs, applications, saved jobs and notifications are protected APIs.
                .requestMatchers("/api/companies/**", "/api/jobs/**", "/api/applications/**", "/api/saved-jobs/**", "/api/notifications/**", "/api/conversations/**", "/api/recommendations/**").authenticated()
                // Keep other routes open for now (you can tighten this later).
                .anyRequest().permitAll()
            )
            // Return 401 for unauthenticated requests; keep 403 for access denied.
            .exceptionHandling(ex -> ex.authenticationEntryPoint(unauthorizedEntryPoint))
            // Validate JWT before Spring's username/password auth filter.
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        // Allow Vite dev server.
        config.setAllowedOrigins(List.of("http://localhost:5173"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
