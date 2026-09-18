package com.mbls.challenge.pokemon.catalog.adapter.out.pokeapi;

import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Exercises the exact "pokeApi" Retry/CircuitBreaker instances that {@link
 * PokeApiClientAdapter} is annotated with, configured from application.yml,
 * to prove the resilience behaviour end to end rather than just trusting
 * the YAML: a transient failure is retried and eventually succeeds, a
 * sustained outage trips the circuit breaker to fail fast, and a genuine
 * 404 counts against neither - it is a normal outcome, not a dependency
 * failure.
 */
@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:pokeapi_resilience_test;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "app.jwt.secret=test-only-secret-must-be-at-least-32-bytes-long"
})
class PokeApiResilienceConfigurationTest {

    @Autowired
    private RetryRegistry retryRegistry;

    @Autowired
    private CircuitBreakerRegistry circuitBreakerRegistry;

    private static HttpClientErrorException.NotFound aNotFoundResponse() {
        return (HttpClientErrorException.NotFound) HttpClientErrorException.create(
                HttpStatus.NOT_FOUND, "Not Found", HttpHeaders.EMPTY, new byte[0], null);
    }

    @Test
    void retryAbsorbsATransientFailureAndEventuallySucceeds() {
        Retry retry = retryRegistry.retry("pokeApi");
        AtomicInteger attempts = new AtomicInteger();

        Supplier<String> flakyThenOk = () -> {
            if (attempts.incrementAndGet() < 3) {
                throw new ResourceAccessException("simulated timeout");
            }
            return "ok";
        };

        String result = Retry.decorateSupplier(retry, flakyThenOk).get();

        assertThat(result).isEqualTo("ok");
        assertThat(attempts.get()).isEqualTo(3); // 2 failures + 1 success, matching max-attempts: 3
    }

    @Test
    void retryGivesUpAfterMaxAttemptsAndPropagatesTheFailure() {
        Retry retry = retryRegistry.retry("pokeApi");
        AtomicInteger attempts = new AtomicInteger();

        Supplier<String> alwaysFails = () -> {
            attempts.incrementAndGet();
            throw new ResourceAccessException("simulated outage");
        };

        assertThatThrownBy(() -> Retry.decorateSupplier(retry, alwaysFails).get())
                .isInstanceOf(ResourceAccessException.class);

        assertThat(attempts.get()).isEqualTo(3);
    }

    @Test
    void retryDoesNotRetryAGenuine404() {
        Retry retry = retryRegistry.retry("pokeApi");
        AtomicInteger attempts = new AtomicInteger();

        Supplier<String> notFound = () -> {
            attempts.incrementAndGet();
            throw aNotFoundResponse();
        };

        assertThatThrownBy(() -> Retry.decorateSupplier(retry, notFound).get())
                .isInstanceOf(HttpClientErrorException.NotFound.class);

        assertThat(attempts.get()).isEqualTo(1);
    }

    @Test
    void circuitBreakerOpensAfterSustainedFailuresAndThenFailsFastWithoutCallingThrough() {
        CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker("pokeApi");
        circuitBreaker.reset(); // isolate from any other test's effect on this shared, named instance

        AtomicInteger callCount = new AtomicInteger();
        Supplier<String> alwaysFails = () -> {
            callCount.incrementAndGet();
            throw new ResourceAccessException("simulated outage");
        };

        // minimum-number-of-calls: 5, failure-rate-threshold: 50 -> 5 straight failures trips it.
        for (int i = 0; i < 5; i++) {
            assertThatThrownBy(() -> CircuitBreaker.decorateSupplier(circuitBreaker, alwaysFails).get())
                    .isInstanceOf(ResourceAccessException.class);
        }
        assertThat(circuitBreaker.getState()).isEqualTo(CircuitBreaker.State.OPEN);

        int callsSoFar = callCount.get();
        assertThatThrownBy(() -> CircuitBreaker.decorateSupplier(circuitBreaker, alwaysFails).get())
                .isInstanceOf(CallNotPermittedException.class);

        // The rejection must be fail-fast: the underlying operation was never invoked again.
        assertThat(callCount.get()).isEqualTo(callsSoFar);
    }

    @Test
    void circuitBreakerDoesNotCountA404AsAFailure() {
        CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker("pokeApi");
        circuitBreaker.reset();

        Supplier<String> notFound = () -> {
            throw aNotFoundResponse();
        };

        for (int i = 0; i < 20; i++) {
            assertThatThrownBy(() -> CircuitBreaker.decorateSupplier(circuitBreaker, notFound).get())
                    .isInstanceOf(HttpClientErrorException.NotFound.class);
        }

        assertThat(circuitBreaker.getState()).isEqualTo(CircuitBreaker.State.CLOSED);
    }
}
