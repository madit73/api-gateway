package com.madi.gateway.filters;


import com.madi.common.utility.auth.constant.GlobalAuthConstants;
import com.madi.common.utility.auth.test.utiltities.TokenHelper;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.wiremock.spring.EnableWireMock;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;

@EnableWireMock
@ActiveProfiles("route")
@AutoConfigureWebTestClient
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class RouteTest
{

    @Autowired
    private WebTestClient client;

    @Value("${limitless.gw.jwt.secret}")
    private String rawSecret;

    private String jwt;

    private final Base64.Encoder encoder = Base64.getEncoder();

    @BeforeEach
    void setUp()
    {
        String encodedSecret = encoder.encodeToString(rawSecret.getBytes(StandardCharsets.UTF_8));
        jwt = TokenHelper.createToken(50, encodedSecret);

        stubFor(get(urlEqualTo("/test"))
            .willReturn(aResponse()
                .withBody("{\"headers\":{\"Hello\":\"World\"}}")
                .withHeader("Content-Type", "application/json")));
    }

    @Test
    void otherService()
    {
        stubFor(get(urlEqualTo("/mads/portal/signout"))
            .willReturn(aResponse().withStatus(200)));

        client.get().uri("/mads/portal/signout")
              .header(GlobalAuthConstants.AUTHORIZATION_HEADER, GlobalAuthConstants.BEARER + jwt)
              .exchange()
              .expectStatus().isOk();
    }
}
