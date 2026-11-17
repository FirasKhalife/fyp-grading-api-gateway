package com.fypgrading.apigateway.routes;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/** Adds {@code X-Response-Time: <ms>}, the time spent behind the gateway (it used to be a timestamp). */
@Component
public class ResponseTimeFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        long start = System.nanoTime();
        exchange.getResponse().beforeCommit(() -> {
            long millis = (System.nanoTime() - start) / 1_000_000;
            exchange.getResponse().getHeaders().set("X-Response-Time", millis + "ms");
            return Mono.empty();
        });
        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
