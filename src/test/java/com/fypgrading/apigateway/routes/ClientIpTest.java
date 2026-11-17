package com.fypgrading.apigateway.routes;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.InetSocketAddress;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;

/** Review H3: the rate-limit key can't be chosen by the caller. */
class ClientIpTest {

    static MockServerHttpRequest from(String peer, String cf, String forwarded) {
        MockServerHttpRequest.BaseBuilder<?> b =
                MockServerHttpRequest.get("/").remoteAddress(new InetSocketAddress(peer, 40000));
        if (cf != null) {
            b.header("CF-Connecting-IP", cf);
        }
        if (forwarded != null) {
            b.header("X-Forwarded-For", forwarded);
        }
        return b.build();
    }

    @Test
    void cloudflaresHeaderIsBelievedOnlyFromATrustedProxy() {
        ClientIp ip = new ClientIp(List.of("172.20.0.0/16"));
        assertThat(ip.of(from("172.20.0.5", "203.0.113.7", null))).isEqualTo("203.0.113.7");
        assertThat(ip.of(from("198.51.100.9", "203.0.113.7", null)))
                .as("a spoofed header from anyone else is ignored")
                .isEqualTo("198.51.100.9");
    }

    @Test
    void forwardedForIsNeverUsed() {
        ClientIp ip = new ClientIp(List.of("172.20.0.0/16"));
        assertThat(ip.of(from("172.20.0.5", null, "203.0.113.7, 10.0.0.1"))).isEqualTo("172.20.0.5");
    }

    @Test
    void withNoTrustedProxyTheSocketAddressIsTheKey() {
        ClientIp ip = new ClientIp(List.of(""));
        assertThat(ip.of(from("192.0.2.9", "203.0.113.7", null))).isEqualTo("192.0.2.9");
    }

    @Test
    void cidrBlocksMatchByPrefix() {
        assertThat(ClientIp.Cidr.parse("10.1.0.0/16").contains(new InetSocketAddress("10.1.200.3", 0).getAddress()))
                .isTrue();
        assertThat(ClientIp.Cidr.parse("10.1.0.0/16").contains(new InetSocketAddress("10.2.0.3", 0).getAddress()))
                .isFalse();
        assertThat(ClientIp.Cidr.parse("192.0.2.9").contains(new InetSocketAddress("192.0.2.9", 0).getAddress()))
                .isTrue();
    }
}
