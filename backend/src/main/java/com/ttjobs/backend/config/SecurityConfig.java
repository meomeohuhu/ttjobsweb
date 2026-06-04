package com.ttjobs.backend.config;

import com.ttjobs.backend.security.JwtFilter;
import jakarta.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
public class SecurityConfig {

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
            "http://127.0.0.1:5190"
    );

    private static final List<String> ALLOWED_METHODS = List.of(
            "GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"
    );

    private final JwtFilter jwtFilter;
    private final String configuredAllowedOrigins;
    private final String appBaseUrl;

    public SecurityConfig(
            JwtFilter jwtFilter,
            @Value("${ttjobs.cors.allowed-origins:}") String configuredAllowedOrigins,
            @Value("${ttjobs.app.base-url:}") String appBaseUrl) {
        this.jwtFilter = jwtFilter;
        this.configuredAllowedOrigins = configuredAllowedOrigins;
        this.appBaseUrl = appBaseUrl;
    }

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
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                // Public endpoints: registration and login.
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/ws/**").permitAll()
                // Docker healthcheck is internal; Nginx blocks public actuator access.
                .requestMatchers("/actuator/health").permitAll()
                // Swagger UI, static OpenAPI spec, and other actuator endpoints are admin-only.
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/openapi.yaml", "/actuator/**").hasRole("ADMIN")
                // Public job listings and job detail.
                .requestMatchers(HttpMethod.GET, "/api/jobs/**").permitAll()
                // Public company pages.
                .requestMatchers(HttpMethod.GET,
                        "/api/companies",
                        "/api/companies/top-saved-jobs",
                        "/api/companies/*",
                        "/api/companies/*/jobs",
                        "/api/companies/*/reviews",
                        "/api/companies/*/public-page",
                        "/api/companies/*/follow-status").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/career-guides/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/skills").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/forum/posts", "/api/forum/posts/**").permitAll()
                .requestMatchers("/api/forum/**").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/tools/salary-benchmark").permitAll()
                .requestMatchers("/api/tools/sessions/**").authenticated()
                // Authenticated company follow actions.
                .requestMatchers("/api/company-follows/**").authenticated()
                .requestMatchers("/api/job-needs/**", "/api/saved-searches/**", "/api/ai/**", "/api/interviews/**").authenticated()
                // Recruiter dashboard and future recruiter workspace routes.
                .requestMatchers("/api/recruiter/**").authenticated()
                // Only users with ROLE_ADMIN can access admin routes.
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                // Any authenticated user can access user profile routes.
                .requestMatchers("/api/users/**").authenticated()
                // Company, jobs, applications, saved jobs and notifications are protected APIs.
                .requestMatchers("/api/companies/**", "/api/jobs/**", "/api/applications/**", "/api/saved-jobs/**", "/api/notifications/**", "/api/conversations/**", "/api/recommendations/**").authenticated()
                // Deny-by-default posture for routes not explicitly listed above.
                .anyRequest().authenticated()
            )
            // Return 401 for unauthenticated requests; keep 403 for authenticated users without a role.
            .exceptionHandling(ex -> ex
                    .authenticationEntryPoint(unauthorizedEntryPoint)
                    .accessDeniedHandler((request, response, accessDeniedException) ->
                            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Forbidden")))
            // Validate JWT before Spring's username/password auth filter.
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(allowedOriginPatterns());
        config.setAllowedMethods(ALLOWED_METHODS);
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    private List<String> allowedOriginPatterns() {
        List<String> origins = new ArrayList<>(FRONTEND_DEV_ORIGINS);
        if (appBaseUrl != null && !appBaseUrl.isBlank()) {
            origins.add(appBaseUrl.trim());
        }
        if (configuredAllowedOrigins != null && !configuredAllowedOrigins.isBlank()) {
            origins.addAll(Arrays.stream(configuredAllowedOrigins.split(","))
                    .map(String::trim)
                    .filter(origin -> !origin.isEmpty())
                    .toList());
        }
        return origins;
    }
}
