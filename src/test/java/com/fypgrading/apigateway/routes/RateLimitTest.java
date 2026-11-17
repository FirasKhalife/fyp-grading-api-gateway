package com.fypgrading.apigateway.routes;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.ratelimit.AbstractRateLimiter;
import org.springframework.cloud.gateway.filter.ratelimit.RateLimiter;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import reactor.core.publisher.Mono;

/** Review H3 and M9: limited requests get 429, Retry-After and a problem body; copies cost one a minute. */
class RateLimitTest {

    static final class Fixed extends AbstractRateLimiter<Object> {
        private final boolean allowed;

        Fixed(boolean allowed) {
            super(Object.class, "fixed", null);
            this.allowed = allowed;
        }

        @Override
        public Mono<RateLimiter.Response> isAllowed(String routeId, String id) {
            return Mono.just(new RateLimiter.Response(allowed, Map.of("X-RateLimit-Remaining", allowed ? "5" : "0")));
        }
    }

    @Test
    void aDeniedRequestGets429WithRetryAfterAndAProblem() {
        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.put("/api/demo/session"));
        RateLimit.of(new Fixed(false), e -> Mono.just("1.2.3.4"), "demo-copy", 60)
                .filter(exchange, e -> Mono.error(new AssertionError("must not reach the service")))
                .block();
        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS);
        assertThat(exchange.getResponse().getHeaders().getFirst("Retry-After")).isEqualTo("60");
        assertThat(exchange.getResponse().getBodyAsString().block())
                .contains("\"code\":\"rate-limited\"")
                .contains("\"status\":429");
    }

    @Test
    void anAllowedRequestPassesWithTheLimitHeaders() {
        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/api/rubrics"));
        boolean[] reached = {false};
        RateLimit.of(new Fixed(true), e -> Mono.just("1.2.3.4"), "rubric", 1)
                .filter(exchange, e -> {
                    reached[0] = true;
                    return Mono.empty();
                })
                .block();
        assertThat(reached[0]).isTrue();
        assertThat(exchange.getResponse().getHeaders().getFirst("X-RateLimit-Remaining"))
                .isEqualTo("5");
    }

    @Test
    void creatingACopyIsLimitedToOneAMinute() {
        GatewayProperties.Limit heavy = new GatewayProperties.Limit(1, 600, 60);
        assertThat(heavy.retryAfterSeconds()).isEqualTo(60);
        assertThat(heavy.cost()).isEqualTo(60);
        assertThat(new GatewayProperties.Limit(20, 60, null).retryAfterSeconds())
                .isEqualTo(1);
    }
}
