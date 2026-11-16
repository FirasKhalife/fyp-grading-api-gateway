package com.fypgrading.apigateway.routes;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Review M5: a request for a demo copy that doesn't exist (purged at midnight, or never created) gets 404
 * {@code demo-session-not-found}, the signal the app uses to show "your demo was reset", instead of empty 200s from
 * every service and orphan rows from writes. admin-service owns the copies; a known copy is cached until the next
 * midnight (Europe/Paris), so this costs about one call per visitor per day. {@code /api/demo/**} is how a copy gets
 * created, so it isn't checked.
 */
@Component
public class DemoCopyFilter implements GlobalFilter, Ordered {

    static final ZoneId ZONE = ZoneId.of("Europe/Paris");
    static final String RESET = "/api/demo/session/reset";
    private static final Pattern UUID_V4 =
            Pattern.compile("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-4[0-9a-fA-F]{3}-[89abAB][0-9a-fA-F]{3}-[0-9a-fA-F]{12}$");

    private final DemoCopyCheck check;
    private final Clock clock;
    private final Map<UUID, Instant> knownUntil = new ConcurrentHashMap<>();

    public DemoCopyFilter(DemoCopyCheck check, Clock clock) {
        this.check = check;
        this.clock = clock;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getPath().value();
        String header = exchange.getRequest().getHeaders().getFirst("X-Demo-Session");
        if (!path.startsWith("/api/")
                || header == null
                || !UUID_V4.matcher(header.trim()).matches()) {
            return chain.filter(exchange); // the service answers 400 demo-session-required itself
        }
        UUID session = UUID.fromString(header.trim());
        if (path.equals(RESET)) {
            // A reset deletes this id (the copy moves to a new one), so it must be checked again afterwards.
            // Forgotten after the call too, in case a request in flight cached it again meanwhile.
            forget(session);
            return chain.filter(exchange).doFinally(signal -> forget(session));
        }
        if (path.startsWith("/api/demo/")) {
            return chain.filter(exchange);
        }
        Instant now = clock.instant();
        Instant until = knownUntil.get(session);
        if (until != null && now.isBefore(until)) {
            return chain.filter(exchange);
        }
        String authorization = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        return check.exists(session, authorization).flatMap(state -> switch (state) {
            case EXISTS -> {
                knownUntil.put(session, nextMidnight(now));
                yield chain.filter(exchange);
            }
            case MISSING -> notFound(exchange);
            // admin-service unreachable: don't block the visitor, the services still scope by session.
            case UNKNOWN -> chain.filter(exchange);
        });
    }

    static Instant nextMidnight(Instant now) {
        return LocalDate.ofInstant(now, ZONE).plusDays(1).atStartOfDay(ZONE).toInstant();
    }

    private static Mono<Void> notFound(ServerWebExchange exchange) {
        var response = exchange.getResponse();
        response.setStatusCode(HttpStatus.NOT_FOUND);
        response.getHeaders().setContentType(MediaType.APPLICATION_PROBLEM_JSON);
        String body = "{\"type\":\"https://assay.dev/problems/demo-session-not-found\",\"title\":\"Not Found\","
                + "\"status\":404,\"detail\":\"Your demo copy was reset. Start a fresh one.\",\"instance\":"
                + Json.string(exchange.getRequest().getPath().value()) + ",\"code\":\"demo-session-not-found\"}";
        DataBuffer buffer = response.bufferFactory().wrap(body.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }

    /** Before routing, after the edge token check (a WebFilter). */
    @Override
    public int getOrder() {
        return -1;
    }

    /** Forget cached copies after a reset (the old id is gone) or for tests. */
    public void forget(UUID session) {
        knownUntil.remove(session);
    }
}
