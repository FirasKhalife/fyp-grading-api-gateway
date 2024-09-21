package com.fypgrading.apigateway.filter;

import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import org.springframework.http.HttpHeaders;
import java.util.List;

@Component
public class FilterUtility {

    public static final String CORRELATION_ID = "correlation-id";

    public String getCorrelationId(HttpHeaders requestHeaders) {
        List<String> correlationHeader = requestHeaders.get(CORRELATION_ID);
        if (correlationHeader != null && !correlationHeader.isEmpty())
            return correlationHeader.stream().findFirst().get();

        return null;
    }

    public ServerWebExchange setRequestHeader(ServerWebExchange exchange, String name, String value) {
        return exchange.mutate().request(exchange.getRequest().mutate().header(name, value).build()).build();
    }

    public ServerWebExchange setCorrelationId(ServerWebExchange exchange, String correlationId) {
        return this.setRequestHeader(exchange, CORRELATION_ID, correlationId);
    }

}