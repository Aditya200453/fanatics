package com.auth.security.jwt;

import com.auth.entity.AuthUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.Map;

@Service
public class JwtService {

    @Value("${auth.jwt.secret}")
    private String secret;

    @Value("${auth.jwt.issuer:clinic-auth-service}")
    private String issuer;

    @Value("${auth.jwt.expiration-minutes:60}")
    private long expirationMinutes;

    private SecretKey key;

    @PostConstruct
    void init() {
        // HS256 requires sufficiently long secret; enforce at runtime
        byte[] bytes = secret.getBytes(StandardCharsets.UTF_8);
        this.key = Keys.hmacShaKeyFor(bytes);
    }

    public String generate(AuthUser user) {
        Instant now = Instant.now();
        Instant exp = now.plusSeconds(expirationMinutes * 60);

        Map<String, Object> claims = Map.of(
                "role", user.getRole().name(),
                "referenceType", user.getReferenceType().name(),
                "referenceId", user.getReferenceId()
        );

        return Jwts.builder()
                .issuer(issuer)
                .subject(user.getEmail())
                .issuedAt(Date.from(now))
                .expiration(Date.from(exp))
                .claims(claims)
                .signWith(key)
                .compact();
    }

    public Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean isValid(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public String extractEmail(String token) {
        return parseClaims(token).getSubject();
    }
}