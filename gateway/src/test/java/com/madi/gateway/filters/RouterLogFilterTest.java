package com.madi.gateway.filters;


import java.nio.charset.StandardCharsets;
import java.util.Base64;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.wiremock.spring.EnableWireMock;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.madi.limitless.common.utility.auth.constant.GlobalAuthConstants.AUTHORIZATION_HEADER;
import static com.madi.limitless.common.utility.auth.constant.GlobalAuthConstants.BEARER;
import static com.madi.limitless.common.utility.auth.test.utiltities.TokenHelper.createToken;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

@EnableWireMock
@ActiveProfiles("router-log")
@AutoConfigureWebTestClient
@ExtendWith(OutputCaptureExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class RouterLogFilterTest
{
    @Autowired
    private WebTestClient client;

    protected static final Base64.Encoder encoder = Base64.getEncoder();

    protected String encodedSecret;

    @Value("${limitless.gw.jwt.secret}")
    private String rawSecret;

    @BeforeEach
    void setUp()
    {
        this.encodedSecret = encoder.encodeToString(rawSecret.getBytes(StandardCharsets.UTF_8));

        stubFor(get(urlEqualTo("/test"))
            .willReturn(aResponse()
                .withBody("{\"headers\":{\"Hello\":\"World\"}}")
                .withHeader("Content-Type", "application/json")));
    }

    @Test
    void pathRouteWorksAndLogs(CapturedOutput output)
    {
        client.get().uri("/test")
              .header(
                  AUTHORIZATION_HEADER,
                  BEARER +
                      createToken(50, encodedSecret)
              )
              .exchange()
              .expectStatus().isOk()
              .expectBody(String.class)
              .consumeWith(result -> assertThat(result.getResponseBody()).isNotEmpty());
        assertTrue(output.getAll().contains(RouterLogFilter.class.getSimpleName()));
    }
}