package com.clinic.api_gateway;

import com.clinic.gateway.security.JwtUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Encoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

    @Test
    void validateToken_validToken_shouldReturnClaims() {
        // ✅ Create a 256-bit key (32 bytes) and Base64 encode it (matches your JwtUtil expectation)
        byte[] keyBytes = new byte[32];
        Arrays.fill(keyBytes, (byte) 7);
        String secretBase64 = Encoders.BASE64.encode(keyBytes);

        JwtUtil jwtUtil = new JwtUtil(secretBase64);

        // ✅ Create token signed using same key bytes
        SecretKey key = Keys.hmacShaKeyFor(keyBytes);

        String token = Jwts.builder()
                .subject("admin@clinic.com")
                .claim("role", "ADMIN")
                .signWith(key)
                .compact();

        Claims claims = jwtUtil.validateToken(token);

        assertNotNull(claims);
        assertEquals("admin@clinic.com", claims.getSubject());
        assertEquals("ADMIN", claims.get("role", String.class));
    }

    @Test
    void validateToken_invalidToken_shouldThrowException() {
        byte[] keyBytes = new byte[32];
        Arrays.fill(keyBytes, (byte) 7);
        String secretBase64 = Encoders.BASE64.encode(keyBytes);

        JwtUtil jwtUtil = new JwtUtil(secretBase64);

        // ❌ invalid token string
        assertThrows(Exception.class, () -> jwtUtil.validateToken("this.is.not.a.jwt"));
    }
}