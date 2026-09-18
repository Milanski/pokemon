package com.mbls.challenge.pokemon.shared.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

/**
 * Enables Spring's cache abstraction, backed by the default in-memory
 * ConcurrentMapCacheManager. Pokémon reference data from PokéAPI is
 * effectively static, so caching it indefinitely per running instance is
 * enough to avoid hammering the public API.
 *
 * {@code order = HIGHEST_PRECEDENCE} makes the cache aspect wrap *outside*
 * the {@code @Retry}/{@code @CircuitBreaker} aspects on {@code
 * PokeApiClientAdapter} (whose default order is a low positive number, so
 * they'd otherwise wrap outside the cache by default). A cache hit must
 * short-circuit before reaching those - otherwise an open circuit breaker
 * would refuse to serve data that's already sitting in the cache.
 */
@Configuration
@EnableCaching(order = Ordered.HIGHEST_PRECEDENCE)
public class CacheConfig {
}
