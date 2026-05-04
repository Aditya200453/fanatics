package com.clinic.auth.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtUtil {

    private final SecretKey key;
    private final long expiryMs;

    public JwtUtil(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiry-ms}") long expiryMs
    ) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expiryMs = expiryMs;
    }

    // ✅ Generate JWT
    public String generateToken(String email, String role) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expiryMs);

        return Jwts.builder()
                .subject(email)          // ✅ NEW style (not deprecated)
                .claim("role", role)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(key)           // ✅ No SignatureAlgorithm, no warning
                .compact();
    }

    // ✅ Validate JWT
    public Claims parseAndValidate(String token) {
        return Jwts.parser()             // ✅ CORRECT for this style
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}