package com.fypgrading.apigateway.routes;

import com.fypgrading.apigateway.routes.GatewayProperties.Limit;
import com.fypgrading.apigateway.routes.GatewayProperties.ServiceRoute;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import java.time.Clock;
import java.time.Duration;
import java.util.List;
import org.springframework.cloud.circuitbreaker.resilience4j.ReactiveResilience4JCircuitBreakerFactory;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JConfigBuilder;
import org.springframework.cloud.client.circuitbreaker.Customizer;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.GatewayFilterSpec;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpMethod;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

/**
 * Builds every route from {@link GatewayProperties} with the same filters: a circuit breaker with a problem
 * fallback, a retry for idempotent GETs, and a per-IP rate limit. Replaces ten hand-copied route blocks.
 */
@Configuration
public class RouteConfiguration {

    @Bean
    RouteLocator routes(
            RouteLocatorBuilder builder,
            GatewayProperties props,
            KeyResolver clientIp,
            RedisRateLimiter normalLimiter,
            RedisRateLimiter heavyLimiter) {
        RouteLocatorBuilder.Builder routes = builder.routes();
        long normalRetry = props.rateLimit().retryAfterSeconds();
        long heavyRetry = props.heavyLimit().retryAfterSeconds();
        // Order matters: streams and heavy calls are matched before the general prefixes.
        for (ServiceRoute stream : props.streams()) {
            routes.route(
                    stream.id(),
                    r -> r.path(stream.paths().toArray(String[]::new))
                            .filters(f -> f.filter(RateLimit.of(normalLimiter, clientIp, stream.id(), normalRetry)))
                            .uri("lb://" + stream.service()));
        }
        for (ServiceRoute heavy : props.heavy()) {
            routes.route(
                    heavy.id(),
                    r -> r.path(heavy.paths().toArray(String[]::new))
                            .and()
                            .method(methods(heavy))
                            .filters(f -> resilient(f, heavy, heavyLimiter, clientIp, heavyRetry, false))
                            .uri("lb://" + heavy.service()));
        }
        for (ServiceRoute route : props.routes()) {
            routes.route(
                    route.id(),
                    r -> r.path(route.paths().toArray(String[]::new))
                            .filters(f -> resilient(f, route, normalLimiter, clientIp, normalRetry, true))
                            .uri("lb://" + route.service()));
        }
        // Each service's OpenAPI document, for the aggregated Swagger UI at /swagger-ui.html.
        props.routes().stream()
                .map(ServiceRoute::service)
                .distinct()
                .forEach(service -> routes.route(
                        "api-docs-" + service,
                        r -> r.path("/v3/api-docs/" + service)
                                .filters(f -> f.setPath("/v3/api-docs")
                                        .filter(RateLimit.of(normalLimiter, clientIp, "api-docs", normalRetry)))
                                .uri("lb://" + service)));
        return routes.build();
    }

    private static GatewayFilterSpec resilient(
            GatewayFilterSpec f,
            ServiceRoute route,
            RedisRateLimiter limiter,
            KeyResolver clientIp,
            long retryAfterSeconds,
            boolean retryGets) {
        f.filter(RateLimit.of(limiter, clientIp, route.id(), retryAfterSeconds))
                .circuitBreaker(c -> c.setName(route.id()).setFallbackUri("forward:/fallback/" + route.service()));
        if (retryGets) {
            f.retry(r -> r.setRetries(2)
                    .setMethods(HttpMethod.GET)
                    .setBackoff(Duration.ofMillis(100), Duration.ofMillis(800), 2, true));
        }
        return f;
    }

    private static HttpMethod[] methods(ServiceRoute route) {
        List<String> names = route.methods() == null || route.methods().isEmpty()
                ? List.of("GET", "POST", "PUT", "PATCH", "DELETE")
                : route.methods();
        return names.stream().map(HttpMethod::valueOf).toArray(HttpMethod[]::new);
    }

    /** The visitor's IP (see {@link ClientIp}): CF-Connecting-IP only from a trusted proxy. */
    @Bean
    KeyResolver clientIp(GatewayProperties props) {
        ClientIp ip = new ClientIp(props.trustedProxies());
        return exchange -> Mono.just(ip.of(exchange.getRequest()));
    }

    @Bean
    Clock clock() {
        return Clock.systemUTC();
    }

    @Bean
    @LoadBalanced
    WebClient.Builder loadBalancedWebClient() {
        return WebClient.builder();
    }

    @Bean
    DemoCopyCheck demoCopyCheck(@LoadBalanced WebClient.Builder loadBalanced) {
        return DemoCopyCheck.viaAdminService(
                loadBalanced.baseUrl("http://admin-service").build());
    }

    @Bean
    @Primary
    RedisRateLimiter normalLimiter(GatewayProperties props) {
        return limiter(props.rateLimit());
    }

    @Bean
    RedisRateLimiter heavyLimiter(GatewayProperties props) {
        return limiter(props.heavyLimit());
    }

    private static RedisRateLimiter limiter(Limit limit) {
        return new RedisRateLimiter(limit.replenishRate(), limit.burstCapacity(), limit.cost());
    }

    /** Creating a demo copy takes a few seconds, so the time limit is generous; failures open the breaker. */
    @Bean
    Customizer<ReactiveResilience4JCircuitBreakerFactory> circuitBreakerDefaults() {
        return factory -> factory.configureDefault(id -> new Resilience4JConfigBuilder(id)
                .timeLimiterConfig(TimeLimiterConfig.custom()
                        .timeoutDuration(Duration.ofSeconds(20))
                        .build())
                .circuitBreakerConfig(CircuitBreakerConfig.custom()
                        .slidingWindowSize(20)
                        .minimumNumberOfCalls(10)
                        .failureRateThreshold(50)
                        .waitDurationInOpenState(Duration.ofSeconds(10))
                        .build())
                .build());
    }
}
