package com.fypgrading.apigateway.config;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import lombok.AllArgsConstructor;
import org.springframework.cloud.circuitbreaker.resilience4j.ReactiveResilience4JCircuitBreakerFactory;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JConfigBuilder;
import org.springframework.cloud.client.circuitbreaker.Customizer;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Objects;

@AllArgsConstructor
@Configuration
public class RouteConfiguration {

    //TODO: OPTIMIZE AND REFACTOR
    @Bean
    public RouteLocator routesConfig(RouteLocatorBuilder builder){
        return builder.routes()
            /* Config Server */
            .route("config_server_route", r -> r
                .path("/config/**")
                .filters(f -> f
                    .stripPrefix(1) // Removes "/config"
                    .circuitBreaker(config -> config
                        .setName("configServerCircuitBreaker")
                        .setFallbackUri("forward:/fallbacks/config-server"))
                    .retry(retryConfig -> retryConfig
                        .setMethods(HttpMethod.GET)
                        .setRetries(3)
                        .setBackoff(Duration.ofMillis(100), Duration.ofMillis(1000), 2, true))
                    .requestRateLimiter(rateConfig -> rateConfig
                        .setRateLimiter(redisRateLimiter())
                        .setKeyResolver(userKeyResolver())))
                .uri("lb://CONFIG-SERVER"))

            /* Admin Service */
            .route("admin_service_route", r -> r
                .path("/api/admin/**", "/api/auth/**", "/api/grades/**",
                    "/api/reviewers/**", "/api/teams/**")
                .filters(f -> f
                    .circuitBreaker(config -> config
                        .setName("adminServiceCircuitBreaker")
                        .setFallbackUri("forward:/fallbacks/admin-service"))
                    .retry(retryConfig -> retryConfig
                        .setMethods(HttpMethod.GET)
                        .setRetries(3)
                        .setBackoff(Duration.ofMillis(100), Duration.ofMillis(1000), 2, true))
                    .requestRateLimiter(rateConfig -> rateConfig
                        .setRateLimiter(redisRateLimiter())
                        .setKeyResolver(userKeyResolver())))
                .uri("lb://ADMIN-SERVICE"))
            // Actuator
            .route("admin_service_actuator_route", r -> r
                .path("/admin/actuator/**")
                .filters(f -> f
                    .stripPrefix(1) // Removes "/admin"
                    .circuitBreaker(config -> config
                        .setName("adminServiceCircuitBreaker")
                        .setFallbackUri("forward:/fallbacks/admin-server"))
                    .retry(retryConfig -> retryConfig
                        .setMethods(HttpMethod.GET)
                        .setRetries(3)
                        .setBackoff(Duration.ofMillis(100), Duration.ofMillis(1000), 2, true))
                    .requestRateLimiter(rateConfig -> rateConfig
                        .setRateLimiter(redisRateLimiter())
                        .setKeyResolver(userKeyResolver())))
                .uri("lb://ADMIN-SERVICE"))

            /* Evaluation Service */
            .route("evaluation_service_route", r -> r
                .path("/api/evaluations/**")
                .filters(f -> f
                    .circuitBreaker(config -> config
                        .setName("evaluationServiceCircuitBreaker")
                        .setFallbackUri("forward:/fallbacks/evaluation-service"))
                    .retry(retryConfig -> retryConfig
                        .setMethods(HttpMethod.GET)
                        .setRetries(3)
                        .setBackoff(Duration.ofMillis(100), Duration.ofMillis(1000), 2, true))
                    .requestRateLimiter(rateConfig -> rateConfig
                        .setRateLimiter(redisRateLimiter())
                        .setKeyResolver(userKeyResolver())))
                .uri("lb://EVALUATION-SERVICE"))
            // Actuator
            .route("evaluation_service_actuator_route", r -> r
                .path("/evaluation/actuator/**")
                .filters(f -> f
                    .stripPrefix(1) // Removes "/evaluation"
                    .circuitBreaker(config -> config
                        .setName("evaluationServiceCircuitBreaker")
                        .setFallbackUri("forward:/fallbacks/evaluation-service"))
                    .retry(retryConfig -> retryConfig
                        .setMethods(HttpMethod.GET)
                        .setRetries(3)
                        .setBackoff(Duration.ofMillis(100), Duration.ofMillis(1000), 2, true))
                    .requestRateLimiter(rateConfig -> rateConfig
                        .setRateLimiter(redisRateLimiter())
                        .setKeyResolver(userKeyResolver())))
                .uri("lb://EVALUATION-SERVICE"))

            /* Notification Service */
            .route("notification_service_route", r -> r
                .path("/api/notifications/**")
                .filters(f -> f
                    .circuitBreaker(config -> config
                        .setName("notificationServiceCircuitBreaker")
                        .setFallbackUri("forward:/fallbacks/notification-service"))
                    .retry(retryConfig -> retryConfig
                        .setMethods(HttpMethod.GET)
                        .setRetries(3)
                        .setBackoff(Duration.ofMillis(100), Duration.ofMillis(1000), 2, true))
                    .requestRateLimiter(rateConfig -> rateConfig
                        .setRateLimiter(redisRateLimiter())
                        .setKeyResolver(userKeyResolver())))
                .uri("lb://NOTIFICATION-SERVICE"))
            // Actuator
            .route("notification_service_actuator_route", r -> r
                .path("/notification/actuator/**")
                .filters(f -> f
                    .stripPrefix(1) // Removes "/notification"
                    .circuitBreaker(config -> config
                        .setName("notificationServiceCircuitBreaker")
                        .setFallbackUri("forward:/fallbacks/notification-service"))
                    .retry(retryConfig -> retryConfig
                        .setMethods(HttpMethod.GET)
                        .setRetries(3)
                        .setBackoff(Duration.ofMillis(100), Duration.ofMillis(1000), 2, true)))
                .uri("lb://NOTIFICATION-SERVICE"))

            /* Rubric Service */
            .route("rubric_service_route", r -> r
                .path("/api/rubrics/**")
                .filters(f -> f
                    .circuitBreaker(config -> config
                        .setName("rubricServiceCircuitBreaker")
                        .setFallbackUri("forward:/fallbacks/rubric-service"))
                    .retry(retryConfig -> retryConfig
                        .setMethods(HttpMethod.GET)
                        .setRetries(3)
                        .setBackoff(Duration.ofMillis(100), Duration.ofMillis(1000), 2, true))
                    .requestRateLimiter(rateConfig -> rateConfig
                        .setRateLimiter(redisRateLimiter())
                        .setKeyResolver(userKeyResolver())))
                .uri("lb://RUBRIC-SERVICE"))
            // Actuator
            .route("rubric_service_actuator_route", r -> r
                .path("/rubric/actuator/**")
                .filters(f -> f
                    .stripPrefix(1) // Removes "/rubric"
                    .circuitBreaker(config -> config
                        .setName("rubricServiceCircuitBreaker")
                        .setFallbackUri("forward:/fallbacks/rubric-service"))
                    .retry(retryConfig -> retryConfig
                        .setMethods(HttpMethod.GET)
                        .setRetries(3)
                        .setBackoff(Duration.ofMillis(100), Duration.ofMillis(1000), 2, true))
                    .requestRateLimiter(rateConfig -> rateConfig
                        .setRateLimiter(redisRateLimiter())
                        .setKeyResolver(userKeyResolver())))
                .uri("lb://RUBRIC-SERVICE"))

            /* API Gateway */
            .route("api_gateway_route", r -> r
                .path("/gateway/**")
                .filters(f -> f
                    .stripPrefix(1) // Removes "/gateway"
                    .circuitBreaker(config -> config
                        .setName("rubricServiceCircuitBreaker")
                        .setFallbackUri("forward:/fallbacks/rubric-service"))
                    .retry(retryConfig -> retryConfig
                        .setMethods(HttpMethod.GET)
                        .setRetries(3)
                        .setBackoff(Duration.ofMillis(100), Duration.ofMillis(1000), 2, true))
                    .requestRateLimiter(rateConfig -> rateConfig
                        .setRateLimiter(redisRateLimiter())
                        .setKeyResolver(userKeyResolver())))
                .uri("lb://API-GATEWAY"))
            .build();
    }

    @Bean
    public Customizer<ReactiveResilience4JCircuitBreakerFactory> defaultCustomizer() {
        return factory -> factory.configureDefault(id -> new Resilience4JConfigBuilder(id)
            .circuitBreakerConfig(CircuitBreakerConfig.ofDefaults())
            .timeLimiterConfig(TimeLimiterConfig.custom().timeoutDuration(Duration.ofSeconds(5)).build())
            .build());
    }

    @Bean
    public RedisRateLimiter redisRateLimiter() {
        return new RedisRateLimiter(3, 3, 3);
    }

    //TODO: Look into preventing DDoS, the resolver below only looks at every individual hostname
    @Bean
    public KeyResolver userKeyResolver() {
        return exchange -> Mono.just(Objects.requireNonNull(exchange.getRequest().getRemoteAddress()).getHostName()) ;
    }
}
