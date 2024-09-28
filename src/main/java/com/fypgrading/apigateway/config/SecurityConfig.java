package com.fypgrading.apigateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverterAdapter;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Arrays;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Value("${spring.security.cors.allowed-origins}")
    private String corsAllowedOrigins;
    @Value("${spring.security.cors.allowed-methods}")
    private String corsAllowedMethods;
    @Value("${spring.security.cors.allowed-headers}")
    private String corsAllowedHeaders;

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity serverHttpSecurity) {
        return serverHttpSecurity.authorizeExchange(exchanges -> exchanges
                .pathMatchers("/api/admin/**").hasAuthority("ADMIN")
                .anyExchange().authenticated())
            .oauth2ResourceServer(oAuth2ResourceServerSpec -> oAuth2ResourceServerSpec
                .jwt(jwtSpec -> jwtSpec.jwtAuthenticationConverter(grantedAuthoritiesExtractor())))
            .cors(corsSpec -> corsSpec.configurationSource(this::corsConfiguration))
            .csrf(ServerHttpSecurity.CsrfSpec::disable)
            .build();
    }

    private Converter<Jwt, Mono<AbstractAuthenticationToken>> grantedAuthoritiesExtractor() {
        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(new AuthoritiesConverter());
        return new ReactiveJwtAuthenticationConverterAdapter(jwtAuthenticationConverter);
    }

    private CorsConfiguration corsConfiguration(ServerWebExchange exchange) {
        CorsConfiguration corsConfig = new CorsConfiguration();
        corsConfig.setAllowedOriginPatterns(Arrays.asList(corsAllowedOrigins.split(",")));
        corsConfig.setAllowedMethods(Arrays.asList(corsAllowedMethods.split(",")));
        corsConfig.setAllowedHeaders(Arrays.asList(corsAllowedHeaders.split(",")));
        corsConfig.setAllowCredentials(true);
        return corsConfig;
    }

}
