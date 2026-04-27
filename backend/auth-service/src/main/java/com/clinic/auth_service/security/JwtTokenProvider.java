package com.clinic.auth_service.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtTokenProvider {

    // For now hardcoded; later move to application.yml
    private final SecretKey key = Keys.secretKeyFor(SignatureAlgorithm.HS256);
    private final long expiryMs = 60 * 60 * 1000; // 1 hour

    public String generateToken(Integer userId, String loginIdentifier, String role) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expiryMs);

        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .claim("login", loginIdentifier)
                .claim("role", role)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(key)
                .compact();
    }

    public SecretKey getKey() {
        return key;
    }
}