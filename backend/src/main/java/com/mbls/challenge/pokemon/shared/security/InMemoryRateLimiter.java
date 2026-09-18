package com.mbls.challenge.pokemon.shared.security;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Deque;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * A sliding-window rate limiter keyed by an arbitrary string (e.g. "client
 * IP + endpoint"). In-memory and per-instance: fine for a single backend
 * instance, and a deliberately simple alternative to a CAPTCHA. Would need a
 * shared store (e.g. Redis) to remain correct behind multiple instances.
 *
 * Entries are pruned lazily on access; a key that is never touched again
 * (e.g. a one-off client IP) keeps a small, empty-ish deque around for the
 * life of the JVM. Acceptable at this scale - see the README.
 */
@Component
class InMemoryRateLimiter {

    private final RateLimitProperties properties;
    private final ConcurrentHashMap<String, Deque<Instant>> requestTimestampsByKey = new ConcurrentHashMap<>();

    InMemoryRateLimiter(RateLimitProperties properties) {
        this.properties = properties;
    }

    /** Returns true if the request identified by {@code key} is within its rate limit. */
    boolean tryAcquire(String key) {
        Instant now = Instant.now();
        Instant windowStart = now.minusSeconds(properties.windowSeconds());
        Deque<Instant> timestamps = requestTimestampsByKey.computeIfAbsent(key, k -> new ConcurrentLinkedDeque<>());

        synchronized (timestamps) {
            while (!timestamps.isEmpty() && timestamps.peekFirst().isBefore(windowStart)) {
                timestamps.pollFirst();
            }
            if (timestamps.size() >= properties.maxRequests()) {
                return false;
            }
            timestamps.addLast(now);
            return true;
        }
    }
}
