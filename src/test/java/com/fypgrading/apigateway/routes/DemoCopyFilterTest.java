package com.fypgrading.apigateway.routes;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import reactor.core.publisher.Mono;

/** Review M5: a purged or unknown demo copy gets 404 demo-session-not-found at the edge. */
class DemoCopyFilterTest {

    static final UUID SESSION = UUID.fromString("5f8ea018-a5d9-4241-8ed0-3146ff6c2534");

    static MockServerWebExchange call(String path) {
        return MockServerWebExchange.from(MockServerHttpRequest.get(path).header("X-Demo-Session", SESSION.toString()));
    }

    static DemoCopyFilter answering(DemoCopyCheck.State state) {
        return new DemoCopyFilter((s, a) -> Mono.just(state), Clock.systemUTC());
    }

    @Test
    void anUnknownCopyIsReportedAsReset() {
        MockServerWebExchange exchange = call("/api/overview");
        answering(DemoCopyCheck.State.MISSING)
                .filter(exchange, e -> Mono.error(new AssertionError("must not reach the service")))
                .block();
        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(exchange.getResponse().getBodyAsString().block()).contains("\"code\":\"demo-session-not-found\"");
    }

    @Test
    void aKnownCopyIsCheckedOnceThenCachedUntilMidnight() {
        AtomicInteger checks = new AtomicInteger();
        DemoCopyFilter filter = new DemoCopyFilter(
                (s, a) -> {
                    checks.incrementAndGet();
                    return Mono.just(DemoCopyCheck.State.EXISTS);
                },
                Clock.systemUTC());
        for (int i = 0; i < 3; i++) {
            MockServerWebExchange exchange = call("/api/rubrics");
            filter.filter(exchange, e -> Mono.empty()).block();
            assertThat(exchange.getResponse().getStatusCode()).isNull();
        }
        assertThat(checks.get()).isEqualTo(1);
    }

    /** Review M6: after a reset the old id is gone, so the cached "exists" must not outlive it. */
    @Test
    void aResetMakesTheOldIdBeCheckedAgain() {
        DemoCopyCheck.State[] state = {DemoCopyCheck.State.EXISTS};
        DemoCopyFilter filter = new DemoCopyFilter((s, a) -> Mono.just(state[0]), Clock.systemUTC());
        filter.filter(call("/api/rubrics"), e -> Mono.empty()).block();

        MockServerWebExchange reset = MockServerWebExchange.from(
                MockServerHttpRequest.post("/api/demo/session/reset").header("X-Demo-Session", SESSION.toString()));
        filter.filter(reset, e -> Mono.empty()).block();
        state[0] = DemoCopyCheck.State.MISSING;

        MockServerWebExchange after = call("/api/rubrics");
        filter.filter(after, e -> Mono.error(new AssertionError("must not reach the service")))
                .block();
        assertThat(after.getResponse().getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void creatingACopyAndAnUnreachableAdminServiceAreNotBlocked() {
        MockServerWebExchange create = call("/api/demo/session");
        answering(DemoCopyCheck.State.MISSING).filter(create, e -> Mono.empty()).block();
        assertThat(create.getResponse().getStatusCode()).isNull();

        MockServerWebExchange read = call("/api/rubrics");
        answering(DemoCopyCheck.State.UNKNOWN).filter(read, e -> Mono.empty()).block();
        assertThat(read.getResponse().getStatusCode()).isNull();
    }
}
