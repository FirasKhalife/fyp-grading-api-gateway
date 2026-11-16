package com.fypgrading.apigateway.security;

import java.nio.charset.StandardCharsets;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * The edge check: every {@code /api/**} call needs a valid Keycloak token (the services check roles again).
 * Only the persona list, the API docs and the gateway's health are public. Nothing else is reachable.
 */
@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    SecurityWebFilterChain edge(ServerHttpSecurity http) {
        return http.csrf(ServerHttpSecurity.CsrfSpec::disable)
                .cors(c -> {})
                .authorizeExchange(auth -> auth.pathMatchers(HttpMethod.OPTIONS)
                        .permitAll()
                        .pathMatchers(HttpMethod.GET, "/api/demo/personas")
                        .permitAll()
                        .pathMatchers("/actuator/health/**", "/actuator/info", "/actuator/prometheus")
                        .permitAll()
                        .pathMatchers("/v3/api-docs/**", "/swagger-ui.html", "/swagger-ui/**", "/webjars/**")
                        .permitAll()
                        .pathMatchers("/fallback/**")
                        .permitAll()
                        .pathMatchers("/api/**")
                        .authenticated()
                        .anyExchange()
                        .denyAll())
                .oauth2ResourceServer(rs -> rs.jwt(jwt -> {})
                        .authenticationEntryPoint((exchange, e) -> problem(
                                exchange,
                                HttpStatus.UNAUTHORIZED,
                                "unauthenticated",
                                "Sign in again: the token is missing or expired.")))
                .exceptionHandling(ex -> ex.accessDeniedHandler((exchange, e) -> problem(
                        exchange, HttpStatus.FORBIDDEN, "forbidden", "This address isn't part of the public API.")))
                .build();
    }

    /** The SPA origin(s) only. The demo-session header and SSE's Last-Event-ID are allowed through. */
    @Bean
    CorsConfigurationSource corsConfigurationSource(@Value("${assay.cors.allowed-origins}") List<String> origins) {
        CorsConfiguration cors = new CorsConfiguration();
        cors.setAllowedOriginPatterns(origins);
        cors.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        cors.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-Demo-Session", "Last-Event-ID", "Accept"));
        cors.setExposedHeaders(List.of("Location", "Retry-After", "Content-Disposition", "X-RateLimit-Remaining"));
        cors.setMaxAge(3600L);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cors);
        return source;
    }

    static Mono<Void> problem(ServerWebExchange exchange, HttpStatus status, String code, String detail) {
        var response = exchange.getResponse();
        response.setStatusCode(status);
        response.getHeaders().setContentType(MediaType.APPLICATION_PROBLEM_JSON);
        String body = "{\"type\":\"https://assay.dev/problems/" + code + "\",\"title\":\"" + status.getReasonPhrase()
                + "\",\"status\":" + status.value() + ",\"detail\":\"" + detail + "\",\"instance\":\""
                + exchange.getRequest().getPath().value() + "\",\"code\":\"" + code + "\"}";
        DataBuffer buffer = response.bufferFactory().wrap(body.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }
}
