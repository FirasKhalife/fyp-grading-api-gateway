package com.fypgrading.apigateway.routes;

import java.time.Duration;
import java.util.UUID;
import reactor.core.publisher.Mono;

/** Asks admin-service whether a visitor's demo copy exists and is complete. */
public interface DemoCopyCheck {

    enum State {
        EXISTS,
        MISSING,
        UNKNOWN
    }

    Mono<State> exists(UUID session, String authorization);

    /** The default: {@code GET lb://admin-service/internal/demo-sessions/{id}} with the visitor's token. */
    static DemoCopyCheck viaAdminService(org.springframework.web.reactive.function.client.WebClient admin) {
        return (session, authorization) -> admin.get()
                .uri("/internal/demo-sessions/{s}", session)
                .headers(h -> {
                    if (authorization != null) {
                        h.set("Authorization", authorization);
                    }
                })
                .exchangeToMono(r -> Mono.just(
                        r.statusCode().is2xxSuccessful()
                                ? State.EXISTS
                                : r.statusCode().value() == 404 ? State.MISSING : State.UNKNOWN))
                .timeout(Duration.ofSeconds(3))
                .onErrorReturn(State.UNKNOWN);
    }
}
