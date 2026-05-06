package com.jukebox.backend.service;

import com.jukebox.backend.config.AppProperties;
import com.jukebox.backend.dto.UserProfile;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

    private final AppProperties appProperties;

    public JwtService(AppProperties appProperties) {
        this.appProperties = appProperties;
    }

    public String issueToken(UserProfile user) {
        Key signingKey = buildSigningKey();
        long now = System.currentTimeMillis();
        long ttlMillis = appProperties.getJwtTtlSeconds() * 1000L;

        if (ttlMillis <= 0L) {
            throw new IllegalStateException("JWT TTL must be greater than zero seconds.");
        }

        return Jwts.builder()
                .setSubject(user.getSubject())
                .claim("provider", user.getProvider())
                .claim("name", user.getName())
                .claim("email", user.getEmail())
                .claim("phoneNumber", user.getPhoneNumber())
                .claim("pictureUrl", user.getPictureUrl())
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(now + ttlMillis))
                .signWith(signingKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public UserProfile parseToken(String token) {
        try {
            Jws<Claims> claims = Jwts.parserBuilder()
                    .setSigningKey(buildSigningKey())
                    .build()
                    .parseClaimsJws(token);

            Claims body = claims.getBody();
            return new UserProfile(
                    stringValue(body.get("provider")),
                    body.getSubject(),
                    stringValue(body.get("name")),
                    stringValue(body.get("email")),
                    stringValue(body.get("pictureUrl")),
                    stringValue(body.get("phoneNumber"))
            );
        } catch (RuntimeException exception) {
            throw new IllegalArgumentException("Authorization token is invalid.");
        }
    }

    private Key buildSigningKey() {
        String secret = appProperties.getJwtSecret();

        if (secret == null || secret.trim().isEmpty()) {
            throw new IllegalStateException("JWT secret is not configured. Set APP_JWT_SECRET first.");
        }

        byte[] secretBytes = secret.trim().getBytes(StandardCharsets.UTF_8);

        if (secretBytes.length < 32) {
            throw new IllegalStateException("JWT secret must be at least 32 bytes long for HS256.");
        }

        return Keys.hmacShaKeyFor(secretBytes);
    }

    private String stringValue(Object value) {
        return value == null ? null : String.valueOf(value);
    }
}
