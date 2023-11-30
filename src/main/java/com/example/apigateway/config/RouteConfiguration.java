package com.example.apigateway.config;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import org.springframework.cloud.circuitbreaker.resilience4j.ReactiveResilience4JCircuitBreakerFactory;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JConfigBuilder;
import org.springframework.cloud.client.circuitbreaker.Customizer;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.CrossOrigin;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Objects;

@Configuration
@CrossOrigin(origins = "*", maxAge = 3600)
public class RouteConfiguration {

    @Bean
    public RouteLocator adminServiceRoute(RouteLocatorBuilder builder){

        return builder.routes().build();
    }

    @Bean
    public RouteLocator reviewerServiceRoute(RouteLocatorBuilder builder){

        return builder.routes().build();
    }

    @Bean
    public RouteLocator adminServiceCircuitBreaker(RouteLocatorBuilder builder){
        return builder.routes()
                .route(r -> r.path("/api/admin/**")
                        .filters(f -> f
                                .circuitBreaker(config -> config
                                        .setName("adminServiceCircuitBreaker")
                                        .setFallbackUri("forward:/fallback/adminServiceGradesFallback"))
                                .requestRateLimiter(config -> config
                                        .setRateLimiter(redisRateLimiter())
                                        .setKeyResolver(userKeyResolver()))
                        )
                        .uri("lb://ADMIN-SERVICE"))
                .route(r -> r.path("/api/auth/login")
                        .filters(f -> f
                                .circuitBreaker(config -> config
                                        .setName("adminServiceAuthenticationCircuitBreaker")
                                        .setFallbackUri("forward:/fallback/adminServiceAuthenticationFallback"))
                                .requestRateLimiter(config -> config
                                        .setRateLimiter(redisRateLimiter())
                                        .setKeyResolver(userKeyResolver()))
                        )
                        .uri("lb://ADMIN-SERVICE"))
                .route(r -> r.path("/api/auth/signup")
                        .filters(f -> f
                                .circuitBreaker(config -> config
                                        .setName("adminServiceAuthenticationCircuitBreaker")
                                        .setFallbackUri("forward:/fallback/adminServiceAuthenticationFallback"))
                                .requestRateLimiter(config -> config
                                        .setRateLimiter(redisRateLimiter())
                                        .setKeyResolver(userKeyResolver()))
                        )
                        .uri("lb://ADMIN-SERVICE"))
                .route(r -> r.path("/api/reviewers/**")
                        .filters(f -> f
                                .circuitBreaker(config -> config
                                    .setName("adminServiceReviewersCircuitBreaker")
                                    .setFallbackUri("forward:/fallback/adminServiceReviewersFallback"))
                                .requestRateLimiter(config -> config
                                        .setRateLimiter(redisRateLimiter())
                                        .setKeyResolver(userKeyResolver()))
                        )
                        .uri("lb://ADMIN-SERVICE"))
                .route(r -> r.path("/api/grades/**")
                        .filters(f -> f
                                .circuitBreaker(config -> config
                                    .setName("adminServiceReviewerGradesCircuitBreaker")
                                    .setFallbackUri("forward:/fallback/adminServiceReviewerGradesFallback"))
                                .requestRateLimiter(config -> config
                                        .setRateLimiter(redisRateLimiter())
                                        .setKeyResolver(userKeyResolver()))
                        )
                        .uri("lb://ADMIN-SERVICE"))
                .route(r -> r.path("/api/teams/**")
                        .filters(f -> f
                                .circuitBreaker(config -> config
                                        .setName("adminServiceTeamsFallback")
                                        .setFallbackUri("forward:/fallback/adminServiceTeamsFallback"))
                                .requestRateLimiter(config -> config
                                        .setRateLimiter(redisRateLimiter())
                                        .setKeyResolver(userKeyResolver()))
                        )
                        .uri("lb://ADMIN-SERVICE"))
                .build();
    }

    @Bean
    public RouteLocator reviewerServiceCircuitBreaker(RouteLocatorBuilder builder){
        return builder.routes()
                .route(r -> r.path("/api/evaluations/**")
                        .filters(f -> f
                                .circuitBreaker(config -> config
                                    .setName("evaluationServiceCircuitBreaker")
                                    .setFallbackUri("forward:/fallback/evaluationServiceCircuitBreaker"))
                                .requestRateLimiter(config -> config
                                        .setRateLimiter(redisRateLimiter())
                                        .setKeyResolver(userKeyResolver()))
                        )
                        .uri("lb://EVALUATION-SERVICE"))
                .build();
    }

    @Bean
    public RouteLocator notificationServiceCircuitBreaker(RouteLocatorBuilder builder){
        return builder.routes()
                .route(r -> r.path("/api/notifications/**")
                        .filters(f -> f
                                .circuitBreaker(config -> config
                                    .setName("notificationServiceCircuitBreaker")
                                    .setFallbackUri("forward:/fallback/notificationServiceCircuitBreaker"))
                                .requestRateLimiter(config -> config
                                        .setRateLimiter(redisRateLimiter())
                                        .setKeyResolver(userKeyResolver()))
                        )
                        .uri("lb://NOTIFICATION-SERVICE"))
                .build();
    }

    @Bean
    public RouteLocator rubricServiceCircuitBreaker(RouteLocatorBuilder builder){
        return builder.routes()
                .route(r -> r.path("/api/rubrics/**")
                        .filters(f -> f
                                .circuitBreaker(config -> config
                                    .setName("rubricServiceCircuitBreaker")
                                    .setFallbackUri("forward:/fallback/rubricServiceCircuitBreaker"))
                                .requestRateLimiter(config -> config
                                        .setRateLimiter(redisRateLimiter())
                                        .setKeyResolver(userKeyResolver()))
                        )
                        .uri("lb://RUBRIC-SERVICE"))
                .build();
    }

    @Bean
    public RouteLocator defaultCircuitBreaker(RouteLocatorBuilder builder){
        return builder.routes()
                .route(r -> r.path("/")
                        .filters(f -> f
                                .circuitBreaker(config -> config
                                    .setName("defaultCircuitBreaker")
                                    .setFallbackUri("forward:/fallback/defaultCircuitBreaker"))
                                .requestRateLimiter(config -> config
                                        .setRateLimiter(redisRateLimiter()).setKeyResolver(userKeyResolver()))
                        )
                        .uri("http://localhost:8080"))
                .build();
    }


    @Bean
    public Customizer<ReactiveResilience4JCircuitBreakerFactory> defaultCustomizer() {
        return factory -> factory.configureDefault(id -> new Resilience4JConfigBuilder(id)
                .circuitBreakerConfig(CircuitBreakerConfig.ofDefaults())
                .timeLimiterConfig(TimeLimiterConfig.custom().
                        timeoutDuration(Duration.ofSeconds(10)).build()).build());
    }

    @Bean
    public RedisRateLimiter redisRateLimiter() {
        return new RedisRateLimiter(1, 2);
    }

    @Bean
    public KeyResolver userKeyResolver() {
        return exchange -> Mono.just(Objects.requireNonNull(exchange.getRequest().getRemoteAddress()).getHostName()) ;
    }
}
