package com.mbls.challenge.pokemon.shared.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Limits how many requests a single client can make to a rate-limited
 * endpoint within {@code windowSeconds}. Deliberately simple (in-memory,
 * per-instance) rather than a CAPTCHA: it slows down account enumeration and
 * credential stuffing without asking a genuine trainer to solve a puzzle.
 */
@ConfigurationProperties(prefix = "app.rate-limit")
public record RateLimitProperties(int maxRequests, int windowSeconds) {
}
