package com.example.apigateway.config;


import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import org.springframework.cloud.circuitbreaker.resilience4j.ReactiveResilience4JCircuitBreakerFactory;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JConfigBuilder;
import org.springframework.cloud.client.circuitbreaker.Customizer;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
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
                .route(r -> r.path("/api/admin/grades/**")
                        .filters(f -> f.circuitBreaker(config -> config
                                .setName("adminServiceCircuitBreaker")
                                .setFallbackUri("forward:/fallback/adminServiceGradesFallback")))
                        .uri("lb://ADMIN-SERVICE"))
                .route(r -> r.path("/api/auth/**")
                        .filters(f -> f.circuitBreaker(config -> config
                                .setName("adminServieAuthenticationCircuitBreaker")
                                .setFallbackUri("forward:/fallback/adminServieAuthenticationFallback")))
                        .uri("lb://ADMIN-SERVICE"))
                .route(r -> r.path("/api/reviewers/**")
                        .filters(f -> f.circuitBreaker(config -> config
                                .setName("adminServieReviewersCircuitBreaker")
                                .setFallbackUri("forward:/fallback/adminServieReviewersFallback")))
                        .uri("lb://ADMIN-SERVICE"))
                .route(r -> r.path("/api/grades/**")
                        .filters(f -> f.circuitBreaker(config -> config
                                .setName("adminServieReviewerGradesCircuitBreaker")
                                .setFallbackUri("forward:/fallback/adminServieReviewerGradesFallback")))
                        .uri("lb://ADMIN-SERVICE"))
                .route(r -> r.path("/api/rubrics/**")
                        .filters(f -> f.circuitBreaker(config -> config
                                .setName("adminServieRubricsCircuitBreaker")
                                .setFallbackUri("forward:/fallback/adminServieRubricsFallback")))
                        .uri("lb://ADMIN-SERVICE"))
                .route(r -> r.path("/api/teams/**")
                        .filters(f -> f.circuitBreaker(config -> config
                                .setName("adminServieTeamsCircuitBreaker")
                                .setFallbackUri("forward:/fallback/adminServieTeamsFallback")))
                        .uri("lb://ADMIN-SERVICE"))
                .build();
    }

    @Bean
    public RouteLocator reviewerServiceCircuitBreaker(RouteLocatorBuilder builder){
        return builder.routes()
                .route(r -> r.path("/api/evaluations/**")
                        .filters(f -> f.circuitBreaker(config -> config
                                .setName("reviewerServiceCircuitBreaker")
                                .setFallbackUri("forward:/fallback/reviewerServiceFallback")))
                        .uri("lb://REVIEWER-SERVICE"))
                .build();
    }

    @Bean
    public Customizer<ReactiveResilience4JCircuitBreakerFactory> defaultCustomizer() {
        return factory -> factory.configureDefault(id -> new Resilience4JConfigBuilder(id)
                .circuitBreakerConfig(CircuitBreakerConfig.ofDefaults())
                .timeLimiterConfig(TimeLimiterConfig.custom().
                        timeoutDuration(Duration.ofSeconds(2)).build()).build());
    }
}
