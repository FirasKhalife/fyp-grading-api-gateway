package com.fypgrading.apigateway.routes;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.InetSocketAddress;
import org.junit.jupiter.api.Test;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;

class ClientIpTest {

    @Test
    void prefersCloudflareThenTheFirstForwardedHopThenTheSocket() {
        assertThat(RouteConfiguration.clientIpOf(MockServerHttpRequest.get("/")
                .header("CF-Connecting-IP", "203.0.113.7").header("X-Forwarded-For", "10.0.0.1").build()))
                .isEqualTo("203.0.113.7");
        assertThat(RouteConfiguration.clientIpOf(MockServerHttpRequest.get("/")
                .header("X-Forwarded-For", "198.51.100.4, 10.0.0.1").build()))
                .isEqualTo("198.51.100.4");
        assertThat(RouteConfiguration.clientIpOf(MockServerHttpRequest.get("/")
                .remoteAddress(new InetSocketAddress("192.0.2.9", 4000)).build()))
                .isEqualTo("192.0.2.9");
    }
}
