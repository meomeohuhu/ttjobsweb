package com.ttjobs.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // 🔥 rất quan trọng
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/user/**").permitAll() // 🔥 mở endpoint user để test
                .requestMatchers("/api/admin/**").hasRole("ADMIN") // 🔥 chỉ admin mới vào được
                .anyRequest().permitAll() // 🔥 mở hết luôn để test
            );

        return http.build();
    }
}