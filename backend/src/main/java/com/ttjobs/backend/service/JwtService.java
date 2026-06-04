package com.ttjobs.backend.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Arrays;
import java.util.Date;

@Service
public class JwtService {

    private static final String DEFAULT_DEV_SECRET = "dev-only-change-this-jwt-secret-32chars";
    private static final int MIN_SECRET_LENGTH = 32;

    private final String secret;
    private final Environment environment;

    public JwtService(@Value("${jwt.secret}") String secret, Environment environment) {
        this.secret = secret;
        this.environment = environment;
    }

    @PostConstruct
    void validateSecret() {
        if (isRelaxedProfile()) {
            return;
        }
        if (secret == null || secret.isBlank()
                || secret.length() < MIN_SECRET_LENGTH
                || DEFAULT_DEV_SECRET.equals(secret)) {
            throw new IllegalStateException("JWT secret must be configured with a strong production value");
        }
    }

    private boolean isRelaxedProfile() {
        return Boolean.parseBoolean(environment.getProperty(
                "org.springframework.boot.test.context.SpringBootTestContextBootstrapper", "false"))
                || Arrays.stream(environment.getActiveProfiles())
                .anyMatch(profile -> profile.equalsIgnoreCase("dev") || profile.equalsIgnoreCase("test"));
    }

    private Key getSignKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    // Create JWT with user email as subject and role as claim.
    public String generateToken(String email, String role) {
        return Jwts.builder()
                .setSubject(email)
                .claim("role", role)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 20L * 60L * 60L * 1000L))
                .signWith(getSignKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // Parse claims once and reuse for extract methods.
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSignKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public String extractEmail(String token) {
        return extractAllClaims(token).getSubject();
    }

    public String extractRole(String token) {
        return extractAllClaims(token).get("role", String.class);
    }
}
