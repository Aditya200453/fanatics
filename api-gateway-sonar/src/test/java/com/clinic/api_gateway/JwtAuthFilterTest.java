package com.clinic.api_gateway;

import com.clinic.gateway.filter.JwtAuthFilter;
import com.clinic.gateway.security.JwtUtil;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.mock.web.server.MockServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthFilterTest {

    @Mock
    private JwtUtil jwtUtil;

    private JwtAuthFilter filter;

    @BeforeEach
    void setup() {
        filter = new JwtAuthFilter(jwtUtil);
    }

    // Small helper chain that records whether it was called and which exchange it received
    private static class RecordingChain implements GatewayFilterChain {
        boolean called = false;
        ServerWebExchange received;

        @Override
        public Mono<Void> filter(ServerWebExchange exchange) {
            called = true;
            received = exchange;
            return Mono.empty();
        }
    }

    private static MockServerWebExchange exchange(HttpMethod method, String path) {
        MockServerHttpRequest req = MockServerHttpRequest.method(method, path).build();
        return MockServerWebExchange.from(req);
    }

    private static MockServerWebExchange exchangeWithAuth(HttpMethod method, String path, String bearerToken) {
        MockServerHttpRequest req = MockServerHttpRequest.method(method, path)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerToken)
                .build();
        return MockServerWebExchange.from(req);
    }

    // ------------------------------------------------------------
    // 1) OPTIONS preflight should pass through (no auth)
    // ------------------------------------------------------------
    @Test
    void optionsRequest_shouldPassThrough() {
        MockServerWebExchange ex = exchange(HttpMethod.OPTIONS, "/patient/anything");
        RecordingChain chain = new RecordingChain();

        StepVerifier.create(filter.filter(ex, chain))
                .verifyComplete();

        assertTrue(chain.called);
        assertNull(ex.getResponse().getStatusCode()); // should not set 401/403
        verifyNoInteractions(jwtUtil);
    }

    // ------------------------------------------------------------
    // 2) Public endpoints should pass through even without token
    // (example: /auth/login)
    // ------------------------------------------------------------
    @Test
    void publicEndpoint_shouldPassThrough_withoutToken() {
        MockServerWebExchange ex = exchange(HttpMethod.POST, "/auth/login");
        RecordingChain chain = new RecordingChain();

        StepVerifier.create(filter.filter(ex, chain))
                .verifyComplete();

        assertTrue(chain.called);
        assertNull(ex.getResponse().getStatusCode());
        verifyNoInteractions(jwtUtil);
    }

    // ------------------------------------------------------------
    // 3) Protected endpoint without Authorization -> 401
    // ------------------------------------------------------------
    @Test
    void protectedEndpoint_withoutAuthHeader_shouldReturn401() {
        MockServerWebExchange ex = exchange(HttpMethod.GET, "/patient/profile");
        RecordingChain chain = new RecordingChain();

        StepVerifier.create(filter.filter(ex, chain))
                .verifyComplete();

        assertFalse(chain.called);
        assertEquals(HttpStatus.UNAUTHORIZED, ex.getResponse().getStatusCode());
        verifyNoInteractions(jwtUtil);
    }

    // ------------------------------------------------------------
    // 4) Invalid token (JwtUtil throws) -> 401
    // ------------------------------------------------------------
    @Test
    void protectedEndpoint_invalidToken_shouldReturn401() {
        MockServerWebExchange ex = exchangeWithAuth(HttpMethod.GET, "/patient/profile", "BAD_TOKEN");
        RecordingChain chain = new RecordingChain();

        when(jwtUtil.validateToken("BAD_TOKEN")).thenThrow(new RuntimeException("invalid"));

        StepVerifier.create(filter.filter(ex, chain))
                .verifyComplete();

        assertFalse(chain.called);
        assertEquals(HttpStatus.UNAUTHORIZED, ex.getResponse().getStatusCode());
        verify(jwtUtil, times(1)).validateToken("BAD_TOKEN");
    }

    // ------------------------------------------------------------
    // 5) Valid token but role NOT allowed for path -> 403
    // example: PATIENT trying /doctor/**
    // ------------------------------------------------------------
    @Test
    void doctorPath_patientRole_shouldReturn403() {
        MockServerWebExchange ex = exchangeWithAuth(HttpMethod.GET, "/doctor/me", "GOOD_TOKEN");
        RecordingChain chain = new RecordingChain();

        Claims claims = mock(Claims.class);
        when(claims.getSubject()).thenReturn("user@clinic.com");
        when(claims.get("role", String.class)).thenReturn("PATIENT");
        when(jwtUtil.validateToken("GOOD_TOKEN")).thenReturn(claims);

        StepVerifier.create(filter.filter(ex, chain))
                .verifyComplete();

        assertFalse(chain.called);
        assertEquals(HttpStatus.FORBIDDEN, ex.getResponse().getStatusCode());
        verify(jwtUtil, times(1)).validateToken("GOOD_TOKEN");
    }

    // ------------------------------------------------------------
    // 6) Valid token, allowed role -> should pass through
    // and must inject X-User-Email & X-User-Role headers
    // ------------------------------------------------------------
    @Test
    void allowedRole_shouldPassThrough_andInjectHeaders() {
        MockServerWebExchange ex = exchangeWithAuth(HttpMethod.GET, "/doctor/me", "GOOD_TOKEN");
        RecordingChain chain = new RecordingChain();

        Claims claims = mock(Claims.class);
        when(claims.getSubject()).thenReturn("admin@clinic.com");
        when(claims.get("role", String.class)).thenReturn("ADMIN");
        when(jwtUtil.validateToken("GOOD_TOKEN")).thenReturn(claims);

        StepVerifier.create(filter.filter(ex, chain))
                .verifyComplete();

        assertTrue(chain.called);
        assertNull(ex.getResponse().getStatusCode());

        // Verify the downstream exchange received injected headers
        assertNotNull(chain.received);

        String forwardedEmail = chain.received.getRequest().getHeaders().getFirst("X-User-Email");
        String forwardedRole  = chain.received.getRequest().getHeaders().getFirst("X-User-Role");

        assertEquals("admin@clinic.com", forwardedEmail);
        assertEquals("ADMIN", forwardedRole);

        verify(jwtUtil, times(1)).validateToken("GOOD_TOKEN");
    }

    // ------------------------------------------------------------
    // 7) Valid token but missing required claims -> 401
    // (email or role blank/null)
    // ------------------------------------------------------------
    @Test
    void missingClaims_shouldReturn401() {
        MockServerWebExchange ex = exchangeWithAuth(HttpMethod.GET, "/patient/profile", "GOOD_TOKEN");
        RecordingChain chain = new RecordingChain();

        Claims claims = mock(Claims.class);
        when(claims.getSubject()).thenReturn("   "); // blank subject
        when(claims.get("role", String.class)).thenReturn("PATIENT");
        when(jwtUtil.validateToken("GOOD_TOKEN")).thenReturn(claims);

        StepVerifier.create(filter.filter(ex, chain))
                .verifyComplete();

        assertFalse(chain.called);
        assertEquals(HttpStatus.UNAUTHORIZED, ex.getResponse().getStatusCode());
    }
}