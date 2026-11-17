package com.fypgrading.apigateway.routes;

import java.nio.charset.StandardCharsets;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.RateLimiter;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import reactor.core.publisher.Mono;

/**
 * Per-IP rate limiting that answers like the contract says (review M9): 429 with {@code Retry-After} and a problem
 * body with code {@code rate-limited}. The stock filter sends an empty 429.
 */
public final class RateLimit {

    private RateLimit() {}

    /**
     * @param retryAfterSeconds how long one request's tokens take to come back ({@code requestedTokens /
     *     replenishRate}, at least 1 s)
     */
    public static GatewayFilter of(RateLimiter<?> limiter, KeyResolver keys, String routeId, long retryAfterSeconds) {
        return (exchange, chain) -> keys.resolve(exchange)
                .defaultIfEmpty("unknown")
                .flatMap(key -> limiter.isAllowed(routeId, key))
                .flatMap(response -> {
                    ServerHttpResponse http = exchange.getResponse();
                    response.getHeaders()
                            .forEach((name, value) -> http.getHeaders().set(name, value));
                    if (response.isAllowed()) {
                        return chain.filter(exchange);
                    }
                    return tooMany(http, exchange.getRequest().getPath().value(), retryAfterSeconds);
                });
    }

    static Mono<Void> tooMany(ServerHttpResponse http, String path, long retryAfterSeconds) {
        http.setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
        http.getHeaders().set(HttpHeaders.RETRY_AFTER, String.valueOf(Math.max(1, retryAfterSeconds)));
        http.getHeaders().setContentType(MediaType.APPLICATION_PROBLEM_JSON);
        String body = "{\"type\":\"https://assay.dev/problems/rate-limited\",\"title\":\"Too Many Requests\","
                + "\"status\":429,\"detail\":\"Too many requests. Try again in a moment.\",\"instance\":"
                + Json.string(path) + ",\"code\":\"rate-limited\"}";
        DataBuffer buffer = http.bufferFactory().wrap(body.getBytes(StandardCharsets.UTF_8));
        return http.writeWith(Mono.just(buffer));
    }
}
