package com.fypgrading.apigateway;

import static org.assertj.core.api.Assertions.assertThat;

import com.fypgrading.apigateway.routes.GatewayProperties;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

/** What the internet can reach through the gateway, and what it can't. */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@ActiveProfiles("test")
class EdgeSecurityTest {

    @Autowired
    WebTestClient web;

    @Autowired
    RouteLocator routes;

    @Autowired
    GatewayProperties props;

    @Test
    void apiCallsWithoutATokenGetAProblem401() {
        web.get()
                .uri("/api/rubrics")
                .exchange()
                .expectStatus()
                .isUnauthorized()
                .expectBody()
                .jsonPath("$.code")
                .isEqualTo("unauthenticated");
    }

    @Test
    void internalEndpointsActuatorAndTheConfigServerAreNotReachable() {
        for (String path : new String[] {
            "/internal/grades",
            "/admin-service/actuator/heapdump",
            "/config/admin-service/prod",
            "/actuator/env",
            "/actuator/shutdown",
            "/api/auth/login"
        }) {
            int status = web.get()
                    .uri(path)
                    .exchange()
                    .returnResult(String.class)
                    .getStatus()
                    .value();
            assertThat(status).as(path).isIn(401, 403, 404);
        }
    }

    @Test
    void healthIsPublic() {
        web.get().uri("/actuator/health").exchange().expectStatus().isOk();
    }

    @Test
    void routesComeFromConfigurationOnePerServicePlusStreamsHeavyCallsAndDocs() {
        var ids = routes.getRoutes().map(r -> r.getId()).collectList().block();
        assertThat(ids)
                .contains(
                        "admin",
                        "evaluation",
                        "rubric",
                        "notification",
                        "notification-stream",
                        "demo-copy",
                        "api-docs-admin-service");
        assertThat(ids).noneMatch(id -> id.contains("actuator") || id.contains("config"));
        assertThat(props.heavyLimit().replenishRate())
                .isLessThan(props.rateLimit().replenishRate());
    }

    @Test
    void corsAllowsTheSpaWithTheDemoSessionHeader() {
        web.options()
                .uri("/api/rubrics")
                .header("Origin", "http://localhost:5173")
                .header("Access-Control-Request-Method", "PUT")
                .header("Access-Control-Request-Headers", "authorization,x-demo-session")
                .exchange()
                .expectStatus()
                .isOk()
                .expectHeader()
                .valueEquals("Access-Control-Allow-Origin", "http://localhost:5173");
    }
}
