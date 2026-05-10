package com.clinic.gateway.filter;

import com.clinic.gateway.security.JwtUtil;
import io.jsonwebtoken.Claims;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
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
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        final String path = exchange.getRequest().getURI().getPath();
        final HttpMethod method = exchange.getRequest().getMethod();

        // ✅ Allow CORS preflight to pass
        if (HttpMethod.OPTIONS.equals(method)) {
            return chain.filter(exchange);
        }

        // ✅ Public endpoints (no JWT required)
        if (isPublic(path)) {
            return chain.filter(exchange);
        }

        final String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ") || authHeader.length() <= 7) {
            return unauthorized(exchange);
        }

        final String token = authHeader.substring(7).trim();
        if (token.isEmpty()) {
            return unauthorized(exchange);
        }

        try {
            Claims claims = jwtUtil.validateToken(token);

            // ✅ Make them FINAL for lambda usage
            final String email = claims.getSubject();
            final String roleRaw = claims.get("role", String.class);

            if (email == null || email.isBlank() || roleRaw == null || roleRaw.isBlank()) {
                return unauthorized(exchange);
            }

            final String role = roleRaw.toUpperCase();

            // ✅ PATIENT SERVICE
            if (isPath(path, "/patient") &&
                    !(role.equals("ADMIN") || role.equals("PATIENT") || role.equals("STAFF"))) {
                return forbidden(exchange);
            }

            // ✅ DOCTOR SERVICE
            if (isPath(path, "/doctor") &&
                    !(role.equals("ADMIN") || role.equals("DOCTOR") || role.equals("STAFF"))) {
                return forbidden(exchange);
            }

            // ✅ APPOINTMENT
            if (isPath(path, "/appointment") &&
                    !(role.equals("ADMIN") || role.equals("DOCTOR") || role.equals("PATIENT") || role.equals("STAFF"))) {
                return forbidden(exchange);
            }

            // ✅ SPECIALITY
            if (isPath(path, "/speciality") &&
                    !(role.equals("ADMIN") || role.equals("STAFF"))) {
                return forbidden(exchange);
            }

            // ✅ Protect admin approval endpoints
            if (isPath(path, "/auth/admin") && !role.equals("ADMIN")) {
                return forbidden(exchange);
            }


            // ✅ Forward identity to services (remove first to prevent spoofing)
            ServerWebExchange mutated = exchange.mutate()
                    .request(r -> r.headers(headers -> {
                        headers.remove("X-User-Email");
                        headers.remove("X-User-Role");
                        headers.add("X-User-Email", email);
                        headers.add("X-User-Role", role);
                    }))
                    .build();

//            TESTING
//            System.out.println("ROLE = " + role);
//            System.out.println("PATH = " + path);

            return chain.filter(mutated);

        } catch (Exception e) {
            return unauthorized(exchange);
        }
    }

    private boolean isPublic(String path) {
        return path.equals("/auth/login")
                || path.equals("/auth/signup")
                || path.startsWith("/auth/internal/");
    }


    private boolean isPath(String path, String base) {
        return path.equals(base) || path.startsWith(base + "/");
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
        return -100;
    }
}
