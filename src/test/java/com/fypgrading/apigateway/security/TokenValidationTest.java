package com.fypgrading.apigateway.security;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

/** The edge only lets through signed tokens from our Keycloak's public issuer, issued to the Assay app. */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@ActiveProfiles("test")
class TokenValidationTest {

    @DynamicPropertySource
    static void realTokenKeys(DynamicPropertyRegistry registry) {
        registry.add("spring.security.oauth2.resourceserver.jwt.jwk-set-uri", TestTokens::jwkSetUri);
    }

    @Autowired
    WebTestClient web;

    private WebTestClient.ResponseSpec call(String token) {
        return web.get()
                .uri("/api/rubrics")
                .header("Authorization", "Bearer " + token)
                .exchange();
    }

    @Test
    void aSignedTokenFromOurIssuerToOurClientGetsPastTheEdge() {
        // No services run in this test, so the route itself fails; what matters is that the token was accepted.
        int status = call(TestTokens.token(TestTokens.ISSUER, TestTokens.CLIENT, "ADMIN"))
                .returnResult(String.class)
                .getStatus()
                .value();
        assertThat(status).isNotIn(401, 403);
    }

    @Test
    void aTokenFromAnotherIssuerIsRejected() {
        call(TestTokens.token("http://elsewhere.test/realms/assay", TestTokens.CLIENT, "ADMIN"))
                .expectStatus()
                .isUnauthorized()
                .expectBody()
                .jsonPath("$.code")
                .isEqualTo("unauthenticated");
    }

    @Test
    void aTokenIssuedToAnotherClientIsRejected() {
        call(TestTokens.token(TestTokens.ISSUER, "admin-cli", "ADMIN"))
                .expectStatus()
                .isUnauthorized()
                .expectBody()
                .jsonPath("$.code")
                .isEqualTo("unauthenticated");
    }
}
