package com.mbls.challenge.pokemon.shared.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.Set;

/**
 * Throttles the unauthenticated auth endpoints by client IP. Mitigates
 * account enumeration on {@code /api/auth/register} (an attacker probing
 * which email addresses already have an account) and credential stuffing on
 * {@code /api/auth/login}, without requiring a CAPTCHA from genuine users.
 */
@Component
public class RateLimitingFilter extends OncePerRequestFilter {

    private static final Set<String> RATE_LIMITED_PATHS = Set.of("/api/auth/register", "/api/auth/login");

    private final InMemoryRateLimiter rateLimiter;
    private final ObjectMapper objectMapper;

    public RateLimitingFilter(InMemoryRateLimiter rateLimiter, ObjectMapper objectMapper) {
        this.rateLimiter = rateLimiter;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
                                     @NonNull FilterChain filterChain) throws ServletException, IOException {
        String path = request.getRequestURI();

        if (RATE_LIMITED_PATHS.contains(path)) {
            String key = clientIp(request) + ":" + path;
            if (!rateLimiter.tryAcquire(key)) {
                respondTooManyRequests(response, path);
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private String clientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private void respondTooManyRequests(HttpServletResponse response, String path) throws IOException {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.TOO_MANY_REQUESTS,
                "too many requests - please wait before trying again");
        problem.setProperty("timestamp", Instant.now());
        problem.setInstance(java.net.URI.create(path));

        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        objectMapper.writeValue(response.getWriter(), problem);
    }
}
