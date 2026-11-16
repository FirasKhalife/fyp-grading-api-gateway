package com.fypgrading.apigateway.routes;

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Optional;
import org.springframework.http.server.reactive.ServerHttpRequest;

/**
 * The visitor's IP, for rate limiting (review H3). {@code CF-Connecting-IP} is believed only when the TCP peer is a
 * trusted proxy (cloudflared on the Docker network); from anyone else the header is ignored, so it can't be used to
 * dodge the limit or to get another visitor limited. {@code X-Forwarded-For} is never used.
 */
public final class ClientIp {

    private final List<Cidr> trustedProxies;

    public ClientIp(List<String> trustedProxies) {
        this.trustedProxies = trustedProxies == null
                ? List.of()
                : trustedProxies.stream()
                        .filter(s -> s != null && !s.isBlank())
                        .map(Cidr::parse)
                        .toList();
    }

    public String of(ServerHttpRequest request) {
        Optional<InetAddress> peer =
                Optional.ofNullable(request.getRemoteAddress()).map(InetSocketAddress::getAddress);
        String cf = request.getHeaders().getFirst("CF-Connecting-IP");
        if (cf != null && !cf.isBlank() && peer.map(this::trusted).orElse(false)) {
            return cf.trim();
        }
        return peer.map(InetAddress::getHostAddress).orElse("unknown");
    }

    private boolean trusted(InetAddress address) {
        return trustedProxies.stream().anyMatch(c -> c.contains(address));
    }

    /** An address block such as {@code 172.20.0.0/16}, or one address. */
    record Cidr(byte[] network, int prefix) {

        static Cidr parse(String text) {
            String[] parts = text.trim().split("/");
            try {
                byte[] address = InetAddress.getByName(parts[0]).getAddress();
                int prefix = parts.length > 1 ? Integer.parseInt(parts[1]) : address.length * 8;
                return new Cidr(address, prefix);
            } catch (UnknownHostException | NumberFormatException e) {
                throw new IllegalArgumentException("Not an address or CIDR block: " + text, e);
            }
        }

        boolean contains(InetAddress address) {
            byte[] candidate = address.getAddress();
            if (candidate.length != network.length) {
                return false;
            }
            int bits = network.length * 8;
            BigInteger mask = BigInteger.ONE
                    .shiftLeft(bits)
                    .subtract(BigInteger.ONE)
                    .shiftRight(prefix)
                    .not()
                    .and(BigInteger.ONE.shiftLeft(bits).subtract(BigInteger.ONE));
            return new BigInteger(1, candidate).and(mask).equals(new BigInteger(1, network).and(mask));
        }
    }
}
