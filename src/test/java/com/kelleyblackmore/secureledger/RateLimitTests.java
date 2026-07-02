package com.kelleyblackmore.secureledger;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

/**
 * Uses a dedicated low capacity (via @TestPropertySource) so this context has its own
 * fresh in-memory buckets and the limit can be tripped deterministically.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "security.rate-limit.capacity=5",
        "security.rate-limit.refill-period-seconds=60"
})
class RateLimitTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void exceedingRateLimitReturns429WithRetryAfter() throws Exception {
        int capacity = 5;
        int status429Count = 0;
        String retryAfter = null;

        // Fire more requests than the capacity from the same (unauthenticated) client IP.
        for (int i = 0; i < capacity + 5; i++) {
            var result = mockMvc.perform(get("/api/tasks")).andReturn();
            int status = result.getResponse().getStatus();
            if (status == 429) {
                status429Count++;
                if (retryAfter == null) {
                    retryAfter = result.getResponse().getHeader("Retry-After");
                }
            }
        }

        assertThat(status429Count).isGreaterThan(0);
        assertThat(retryAfter).isNotNull();
        assertThat(Integer.parseInt(retryAfter)).isGreaterThanOrEqualTo(1);
    }

    @Test
    void requestsWithinLimitAreNotThrottled() throws Exception {
        // The very first request from a fresh bucket must never be 429.
        var result = mockMvc.perform(get("/healthz")).andReturn();
        assertThat(result.getResponse().getStatus()).isEqualTo(200);
    }
}
