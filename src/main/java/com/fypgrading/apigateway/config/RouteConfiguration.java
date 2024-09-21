package com.fypgrading.apigateway.config;

import lombok.AllArgsConstructor;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.GatewayFilterSpec;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;

@AllArgsConstructor
@Configuration
public class RouteConfiguration {

    private GatewayFilterSpec currentTimeResponseHeaderFilter(GatewayFilterSpec filter) {
        return filter.addResponseHeader("X-Response-Time", LocalDateTime.now().toString());
    }

    //TODO: OPTIMIZE AND REFACTOR
    @Bean
    public RouteLocator routesConfig(RouteLocatorBuilder builder){
        return builder.routes()
            /* Registry Server */
            .route("registry_server_route", r -> r
                .path("/eureka/**")
                .filters(f -> f.stripPrefix(1)) // Removes "/eureka"
                .uri("lb://REGISTRY-SERVER"))
            /* Config Server */
            .route("config_server_route", r -> r
                .path("/config/**")
                .filters(f -> f.stripPrefix(1)) // Removes "/config"
                .uri("lb://CONFIG-SERVER"))
            /* Admin Service */
            .route("admin_service_route", r -> r
                .path("/api/admin/**", "/api/auth/**", "/api/grades/**",
                    "/api/reviewers/**", "/api/teams/**")
                .filters(this::currentTimeResponseHeaderFilter)
                .uri("lb://ADMIN-SERVICE"))
            .route("admin_service_actuator_route", r -> r
                .path("/admin/actuator/**")
                .filters(f -> f.stripPrefix(1)) // Removes "/admin"
                .uri("lb://ADMIN-SERVER"))
            /* Evaluation Service */
            .route("evaluation_service_route", r -> r
                .path("/api/evaluations/**")
                .filters(this::currentTimeResponseHeaderFilter)
                .uri("lb://EVALUATION-SERVICE"))
            .route("evaluation_service_actuator_route", r -> r
                .path("/evaluation/actuator/**")
                .filters(f -> f.stripPrefix(1)) // Removes "/evaluation"
                .uri("lb://EVALUATION-SERVER"))
            /* Notification Service */
            .route("notification_service_route", r -> r
                .path("/api/notifications/**")
                .filters(this::currentTimeResponseHeaderFilter)
                .uri("lb://NOTIFICATION-SERVICE"))
            .route("notification_service_actuator_route", r -> r
                .path("/notification/actuator/**")
                .filters(f -> f.stripPrefix(1)) // Removes "/notification"
                .uri("lb://NOTIFICATION-SERVICE"))
            /* Rubric Service */
            .route("rubric_service_route", r -> r
                .path("/api/rubrics/**")
                .filters(this::currentTimeResponseHeaderFilter)
                .uri("lb://RUBRIC-SERVICE"))
            .route("rubric_service_actuator_route", r -> r
                .path("/rubric/actuator/**")
                .filters(f -> f.stripPrefix(1)) // Removes "/rubric"
                .uri("lb://RUBRIC-SERVICE"))
            /* API Gateway */
            .route("api_gateway_route", r -> r
                .path("/gateway/**")
                .filters(f -> f.stripPrefix(1)) // Removes "/gateway"
                .uri("lb://API-GATEWAY"))
            .build();
    }

//    @Bean
//    public RouteLocator adminServiceCircuitBreaker(RouteLocatorBuilder builder){
//        return builder.routes()
//                .route(r -> r.path("/api/admin/**")
//                        .filters(f -> f
//                                .circuitBreaker(config -> config
//                                        .setName("adminServiceCircuitBreaker")
//                                        .setFallbackUri("forward:/fallback/adminServiceGradesFallback"))
//                                .requestRateLimiter(config -> config
//                                        .setRateLimiter(redisRateLimiter())
//                                        .setKeyResolver(userKeyResolver()))
//                        )
//                        .uri("lb://ADMIN-SERVICE"))
//                .route(r -> r.path("/api/auth/login")
//                        .filters(f -> f
//                                .circuitBreaker(config -> config
//                                        .setName("adminServiceAuthenticationCircuitBreaker")
//                                        .setFallbackUri("forward:/fallback/adminServiceAuthenticationFallback"))
//                                .requestRateLimiter(config -> config
//                                        .setRateLimiter(redisRateLimiter())
//                                        .setKeyResolver(userKeyResolver()))
//                        )
//                        .uri("lb://ADMIN-SERVICE"))
//                .route(r -> r.path("/api/auth/signup")
//                        .filters(f -> f
//                                .circuitBreaker(config -> config
//                                        .setName("adminServiceAuthenticationCircuitBreaker")
//                                        .setFallbackUri("forward:/fallback/adminServiceAuthenticationFallback"))
//                                .requestRateLimiter(config -> config
//                                        .setRateLimiter(redisRateLimiter())
//                                        .setKeyResolver(userKeyResolver()))
//                        )
//                        .uri("lb://ADMIN-SERVICE"))
//                .route(r -> r.path("/api/reviewers/**")
//                        .filters(f -> f
//                                .circuitBreaker(config -> config
//                                    .setName("adminServiceReviewersCircuitBreaker")
//                                    .setFallbackUri("forward:/fallback/adminServiceReviewersFallback"))
//                                .requestRateLimiter(config -> config
//                                        .setRateLimiter(redisRateLimiter())
//                                        .setKeyResolver(userKeyResolver()))
//                        )
//                        .uri("lb://ADMIN-SERVICE"))
//                .route(r -> r.path("/api/grades/**")
//                        .filters(f -> f
//                                .circuitBreaker(config -> config
//                                    .setName("adminServiceReviewerGradesCircuitBreaker")
//                                    .setFallbackUri("forward:/fallback/adminServiceReviewerGradesFallback"))
//                                .requestRateLimiter(config -> config
//                                        .setRateLimiter(redisRateLimiter())
//                                        .setKeyResolver(userKeyResolver()))
//                        )
//                        .uri("lb://ADMIN-SERVICE"))
//                .route(r -> r.path("/api/teams/**")
//                        .filters(f -> f
//                                .circuitBreaker(config -> config
//                                        .setName("adminServiceTeamsFallback")
//                                        .setFallbackUri("forward:/fallback/adminServiceTeamsFallback"))
//                                .requestRateLimiter(config -> config
//                                        .setRateLimiter(redisRateLimiter())
//                                        .setKeyResolver(userKeyResolver()))
//                        )
//                        .uri("lb://ADMIN-SERVICE"))
//                .build();
//    }

//    @Bean
//    public RouteLocator reviewerServiceCircuitBreaker(RouteLocatorBuilder builder){
//        return builder.routes()
//                .route(r -> r.path("/api/evaluations/**")
//                        .filters(f -> f
//                                .circuitBreaker(config -> config
//                                    .setName("evaluationServiceCircuitBreaker")
//                                    .setFallbackUri("forward:/fallback/evaluationServiceCircuitBreaker"))
//                                .requestRateLimiter(config -> config
//                                        .setRateLimiter(redisRateLimiter())
//                                        .setKeyResolver(userKeyResolver()))
//                        )
//                        .uri("lb://EVALUATION-SERVICE"))
//                .build();
//    }

//    @Bean
//    public RouteLocator notificationServiceCircuitBreaker(RouteLocatorBuilder builder){
//        return builder.routes()
//                .route(r -> r.path("/api/notifications/**")
//                        .filters(f -> f
//                                .circuitBreaker(config -> config
//                                    .setName("notificationServiceCircuitBreaker")
//                                    .setFallbackUri("forward:/fallback/notificationServiceCircuitBreaker"))
//                                .requestRateLimiter(config -> config
//                                        .setRateLimiter(redisRateLimiter())
//                                        .setKeyResolver(userKeyResolver()))
//                        )
//                        .uri("lb://NOTIFICATION-SERVICE"))
//                .build();
//    }

//    @Bean
//    public RouteLocator rubricServiceCircuitBreaker(RouteLocatorBuilder builder){
//        return builder.routes()
//                .route(r -> r.path("/api/rubrics/**")
//                        .filters(f -> f
//                                .circuitBreaker(config -> config
//                                    .setName("rubricServiceCircuitBreaker")
//                                    .setFallbackUri("forward:/fallback/rubricServiceCircuitBreaker"))
//                                .requestRateLimiter(config -> config
//                                        .setRateLimiter(redisRateLimiter())
//                                        .setKeyResolver(userKeyResolver()))
//                        )
//                        .uri("lb://RUBRIC-SERVICE"))
//                .build();
//    }

//    @Bean
//    public Customizer<ReactiveResilience4JCircuitBreakerFactory> defaultCustomizer() {
//        return factory -> factory.configureDefault(id -> new Resilience4JConfigBuilder(id)
//                .circuitBreakerConfig(CircuitBreakerConfig.ofDefaults())
//                .timeLimiterConfig(TimeLimiterConfig.custom().
//                        timeoutDuration(Duration.ofSeconds(10)).build()).build());
//    }

//    @Bean
//    public RedisRateLimiter redisRateLimiter() {
//        return new RedisRateLimiter(1, 2);
//    }

//    @Bean
//    public KeyResolver userKeyResolver() {
//        return exchange -> Mono.just(Objects.requireNonNull(exchange.getRequest().getRemoteAddress()).getHostName()) ;
//    }
}
