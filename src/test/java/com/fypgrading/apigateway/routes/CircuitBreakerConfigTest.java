package com.fypgrading.apigateway.routes;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.circuitbreaker.resilience4j.ReactiveResilience4jBulkheadProvider;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;

/**
 * The routes' circuit breaker has no bulkhead. The starter's default one lets 25 calls per route through at a time,
 * for all visitors together, and sends the rest straight to the 503 fallback: one page of the app firing a few dozen
 * calls was enough to hit it.
 */
@SpringBootTest
@ActiveProfiles("test")
class CircuitBreakerConfigTest {

    @Autowired
    ApplicationContext context;

    @Test
    void routesHaveNoBulkhead() {
        assertThat(context.getBeanProvider(ReactiveResilience4jBulkheadProvider.class)
                        .getIfAvailable())
                .isNull();
    }
}
