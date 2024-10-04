package com.fypgrading.apigateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Slf4j
@Configuration
public class ResponseTraceFilter implements GlobalFilter {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, org.springframework.cloud.gateway.filter.GatewayFilterChain chain) {
        log.debug("Adding Response Time to response");
        exchange.getResponse().getHeaders().add("X-Response-Time", String.valueOf(System.currentTimeMillis()));
        return chain.filter(exchange);
    }
}
