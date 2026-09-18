package com.mbls.challenge.pokemon.shared.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Uses its own (low) rate-limit override so it doesn't share a Spring
 * context - and therefore doesn't share the in-memory limiter's state -
 * with tests that hit /api/auth/** for unrelated reasons.
 *
 * Both assertions live in one test method deliberately: the rate limiter's
 * state is a singleton bean shared across every test method that reuses
 * this Spring context (test execution order is not guaranteed), so a second
 * method independently exhausting the same "login" budget would either see
 * stale state from this one or leave stale state for it.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, properties = {
        "spring.datasource.url=jdbc:h2:mem:rate_limiting_filter_test;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "app.jwt.secret=test-only-secret-must-be-at-least-32-bytes-long",
        "app.rate-limit.max-requests=3",
        "app.rate-limit.window-seconds=60"
})
@AutoConfigureMockMvc
class RateLimitingFilterTest {

    @Autowired
    private MockMvc mockMvc;

    private static final String LOGIN_BODY = """
            {"username":"nobody","password":"whatever1"}
            """;

    @Test
    void exceedingTheLimitOnOneEndpointReturns429ButLeavesOtherEndpointsUnaffected() throws Exception {
        for (int i = 0; i < 3; i++) {
            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(LOGIN_BODY))
                    .andExpect(status().isUnauthorized());
        }

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(LOGIN_BODY))
                .andExpect(status().isTooManyRequests());

        // Login's budget is exhausted, but register has its own separate one.
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"freshtrainer","email":"fresh@pallet.town","password":"trainerpw1"}
                                """))
                .andExpect(status().isCreated());
    }
}
