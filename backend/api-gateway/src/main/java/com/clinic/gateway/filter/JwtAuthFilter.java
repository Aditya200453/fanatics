package com.clinic.gateway.filter;

import com.clinic.gateway.security.JwtUtil;
import io.jsonwebtoken.Claims;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class JwtAuthFilter implements GlobalFilter, Ordered {

    private final JwtUtil jwtUtil;

    public JwtAuthFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange,
                             org.springframework.cloud.gateway.filter.GatewayFilterChain chain) {

        String path = exchange.getRequest().getPath().toString();

        // ✅ PUBLIC ENDPOINTS (no JWT required)
        if (
                path.startsWith("/auth/") || path.equals("/patient/signup") || path.equals("/doctor/signup")
        ) {
            return chain.filter(exchange);
        }


        String authHeader = exchange.getRequest()
                .getHeaders()
                .getFirst(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return unauthorized(exchange);
        }

        String token = authHeader.substring(7);

        try {
            Claims claims = jwtUtil.validateToken(token);
            String role = claims.get("role", String.class);

            // ✅ ROLE‑BASED ACCESS RULES
            if (path.startsWith("/patient") &&
                    !(role.equals("ADMIN") || role.equals("PATIENT"))) {
                return forbidden(exchange);
            }

            if (path.startsWith("/doctor") &&
                    !(role.equals("ADMIN") || role.equals("DOCTOR"))) {
                return forbidden(exchange);
            }

            if (path.startsWith("/appointment") &&
                    !(role.equals("ADMIN") || role.equals("DOCTOR") || role.equals("PATIENT"))) {
                return forbidden(exchange);
            }

            // ✅ Forward identity to services
            exchange = exchange.mutate()
                    .request(r -> r
                            .header("X-User-Email", claims.getSubject())
                            .header("X-User-Role", role))
                    .build();

        } catch (Exception e) {
            return unauthorized(exchange);
        }

        return chain.filter(exchange);
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().setComplete();
    }

    private Mono<Void> forbidden(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
        return exchange.getResponse().setComplete();
    }

    @Override
    public int getOrder() {
        return -1;
    }
}