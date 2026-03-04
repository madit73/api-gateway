package com.madi.gateway.filters;


import com.github.tomakehurst.wiremock.stubbing.ServeEvent;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import com.madi.common.utility.auth.constant.GlobalAuthConstants;
import com.madi.common.utility.auth.jwt.JwtHelper;
import com.madi.common.utility.auth.test.utiltities.TokenHelper;
import io.jsonwebtoken.Claims;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.wiremock.spring.EnableWireMock;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getAllServeEvents;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@EnableWireMock
@ActiveProfiles("token")
@AutoConfigureWebTestClient
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AuthorizationGatewayFilterFactoryTest
{
    @Autowired
    private WebTestClient client;

    protected final Base64.Encoder encoder = Base64.getEncoder();

    protected String encodedSecret;

    @Value("${limitless.gw.jwt.secret}")
    protected String rawSecret;

    @BeforeEach
    void setUp()
    {

        this.encodedSecret = encoder.encodeToString(rawSecret.getBytes(StandardCharsets.UTF_8));

        stubFor(get(urlEqualTo("/test"))
            .willReturn(aResponse()
                .withBody("{\"headers\":{\"Hello\":\"World\"}}")
                .withHeader("Content-Type", "application/json")));

        stubFor(get(urlEqualTo("/home"))
            .willReturn(aResponse()
                .withBody("{\"headers\":{\"Hello\":\"Home\"}}")
                .withHeader("Content-Type", "application/json")));

        stubFor(get(urlEqualTo("/another-app/test"))
            .willReturn(aResponse()
                .withBody("{\"headers\":{\"Hello\":\"App\"}}")
                .withHeader("Content-Type", "application/json")));

        stubFor(get(urlEqualTo("/service"))
            .willReturn(aResponse()
                .withBody("{\"headers\":{\"Hello\":\"Service\"}}")
                .withHeader("Content-Type", "application/json")));
    }

    @Test
    void RedirectWhenNoToken()
    {
        client.get().uri("/another-app/test")
              .exchange()
              .expectStatus().isTemporaryRedirect();
    }

    @Test
    void jwtInCookieIsRefreshed()
    {
        String jwt = TokenHelper.createToken(50, encodedSecret);
        client.get()
              .uri("/service")
              .cookie(GlobalAuthConstants.AUTHORIZATION_HEADER, jwt)
              .exchange()
              .expectStatus()
              .isOk()
              .expectHeader()
              .value(
                  GlobalAuthConstants.AUTHORIZATION_HEADER,
                  val -> assertFalse(val.equalsIgnoreCase(GlobalAuthConstants.BEARER + jwt))
              )
              .expectCookie()
              .value(
                  GlobalAuthConstants.AUTHORIZATION_HEADER,
                  val -> assertFalse(val.equalsIgnoreCase(jwt))
              );
    }

    @Test
    void jwtInAuthorizationHeaderIsRefreshed()
    {
        String jwt = TokenHelper.createToken(50, encodedSecret);
        client.get()
              .uri("/service")
              .header(
                  GlobalAuthConstants.AUTHORIZATION_HEADER,
                  GlobalAuthConstants.BEARER + jwt
              )
              .exchange()
              .expectStatus()
              .isOk()
              .expectHeader()
              .value(
                  GlobalAuthConstants.AUTHORIZATION_HEADER,
                  val -> assertFalse(val.equalsIgnoreCase(
                      GlobalAuthConstants.BEARER + jwt))
              )
              .expectCookie()
              .value(
                  GlobalAuthConstants.AUTHORIZATION_HEADER,
                  val -> assertFalse(val.equalsIgnoreCase(jwt))
              );
    }

    @Test
    void expiredJwt()
    {
        //TODO add commented code back in when UI is available
        String jwt = TokenHelper.createToken(-50, encodedSecret);
        client.get().uri("/service")
              .header(GlobalAuthConstants.AUTHORIZATION_HEADER, GlobalAuthConstants.BEARER + jwt)
              .exchange()
              .expectStatus()
              .isTemporaryRedirect();
        //.expectHeader().value("location", containsString("/external"));
    }

    @Test
    void extensionDoesNotSetHeader()
    {
        stubFor(get(urlEqualTo("/service/file.pdf"))
            .willReturn(aResponse()
                .withBody("{\"headers\":{\"Hello\":\"World\"}}")
                .withHeader("Content-Type", "application/json")));
        client.get().uri("/service/file.pdf")
              .exchange()
              .expectStatus().isOk()
              .expectHeader().doesNotExist(GlobalAuthConstants.AUTHORIZATION_HEADER);
    }

    @Test
    void extensionsSetsHeaderIfCookiePresent()
    {
        stubFor(get(urlEqualTo("/service/file.pdf"))
            .willReturn(aResponse()
                .withBody("{\"headers\":{\"Hello\":\"World\"}}")
                .withHeader("Content-Type", "application/json")));
        String jwt = TokenHelper.createToken(50, encodedSecret);
        client.get().uri("/service/file.pdf")
              .cookie(GlobalAuthConstants.AUTHORIZATION_HEADER, jwt)
              .exchange()
              .expectStatus().isOk()
              .expectHeader().exists(GlobalAuthConstants.AUTHORIZATION_HEADER);
    }

    @Test
    void unsupportedExtensionSetsHeader()
    {
        stubFor(get(urlEqualTo("/service/file.xxx"))
            .willReturn(aResponse()
                .withBody("{\"headers\":{\"Hello\":\"World\"}}")
                .withHeader("Content-Type", "application/json")));

        String jwt = TokenHelper.createToken(50, encodedSecret);
        client.get().uri("/service/file.xxx")
              .header(GlobalAuthConstants.AUTHORIZATION_HEADER, GlobalAuthConstants.BEARER + jwt)
              .exchange()
              .expectStatus().isOk()
              .expectHeader().exists(GlobalAuthConstants.AUTHORIZATION_HEADER);
    }

    @Test
    void publicEndpoint()
    {
        stubFor(get(urlEqualTo("/mads/portal/authenticate"))
            .willReturn(aResponse()
                .withBody("{\"headers\":{\"Hello\":\"Login\"}}")
                .withHeader("Content-Type", "application/json")));

        client.get().uri("/mads/portal/authenticate")
              .exchange()
              .expectStatus().isOk()
              .expectHeader().doesNotExist(GlobalAuthConstants.AUTHORIZATION_HEADER)
              .expectCookie().doesNotExist(GlobalAuthConstants.AUTHORIZATION_HEADER);
    }

    @Test
    void jwtInCookieAndHeadersShouldRefreshAll()
    {
        String jwt = TokenHelper.createToken(50, encodedSecret);
        client.get().uri("/service")
              .header(GlobalAuthConstants.AUTHORIZATION_HEADER, GlobalAuthConstants.BEARER + jwt)
              .cookie(GlobalAuthConstants.AUTHORIZATION_HEADER, jwt)
              .exchange()
              .expectStatus().isOk();
        ServeEvent event = getAllServeEvents().getFirst();
        LoggedRequest request = event.getRequest();
        Claims originalClaims = JwtHelper.buildJwtHelperWithEnCodedSecret(encodedSecret, jwt).getClaims();

        String authJwt = request.getHeaders().getHeader(GlobalAuthConstants.AUTHORIZATION_HEADER).firstValue();
        Claims authClaims = JwtHelper.buildJwtHelperWithEnCodedSecret(encodedSecret, authJwt).getClaims();
        assertTrue(authClaims.getExpiration().after(originalClaims.getExpiration()));

        String requestCookie = request.getCookies().get(GlobalAuthConstants.AUTHORIZATION_HEADER).getValue();
        Claims cookieClaims = JwtHelper.buildJwtHelperWithEnCodedSecret(encodedSecret, requestCookie).getClaims();
        assertEquals(cookieClaims.getExpiration(), originalClaims.getExpiration());
    }
}