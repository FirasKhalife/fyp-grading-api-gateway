package com.fypgrading.apigateway.routes;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * The public API, as data: which path prefixes go to which service, plus the rate limits. Everything not listed
 * (actuator, {@code /internal/**}, the config server) is not routed at all.
 *
 * @param routes      one entry per service
 * @param streams     long-lived Server-Sent Event paths: no circuit breaker, retry or timeout
 * @param heavy       expensive calls (creating or resetting a demo copy) with their own, tighter limit
 * @param rateLimit   requests per second and burst, per client IP, for normal calls
 * @param heavyLimit  the same for {@code heavy} paths
 */
@ConfigurationProperties("assay.gateway")
public record GatewayProperties(
        List<ServiceRoute> routes,
        List<ServiceRoute> streams,
        List<ServiceRoute> heavy,
        Limit rateLimit,
        Limit heavyLimit) {

    public record ServiceRoute(String id, String service, List<String> paths, List<String> methods) {}

    public record Limit(int replenishRate, int burstCapacity) {}
}
