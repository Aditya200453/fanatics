package com.clinic.auth.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtUtil {

    private final SecretKey key;
    private final long expiryMs;

    public JwtUtil(
            @Value("${jwt.secret}") String secretBase64,
            @Value("${jwt.expiry-ms}") long expiryMs
    ) {
        byte[] keyBytes = Decoders.BASE64.decode(secretBase64);

        // ✅ HS256 requires >= 32 bytes (256 bits)
        if (keyBytes.length < 32) {
            throw new IllegalArgumentException(
                    "jwt.secret is too short. Use a Base64-encoded key of at least 32 bytes (256 bits)."
            );
        }

        this.key = Keys.hmacShaKeyFor(keyBytes);
        this.expiryMs = expiryMs;
    }

    // ✅ Generate JWT (JJWT 0.12.x style)
    public String generateToken(String email, String role) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expiryMs);

        return Jwts.builder()
                .subject(email)
                .claim("role", role)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(key)
                .compact();
    }

    // ✅ Parse + Validate JWT (JJWT 0.12.x style)
    public Claims parseAndValidate(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}