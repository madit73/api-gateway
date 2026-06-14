package com.madi.gateway.filters;


import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.wiremock.spring.EnableWireMock;
import redis.embedded.RedisServer;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for the Redis-backed rate limiter on the rate-limit route.
 *
 * <p>The test profile sets replenishRate=1 and burstCapacity=2, so each unique
 * X-API-KEY starts with 2 tokens. Tests use distinct keys to avoid cross-test interference in the shared Redis
 * instance.
 *
 * <p>Embedded Redis is started in a static initializer, so it is available before
 * the Spring context is created (Spring connects to Redis during context startup).
 */
@EnableWireMock
@ActiveProfiles("rate-limit")
@AutoConfigureWebTestClient
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class RateLimitingIntegrationTest
{
    private static final String RATE_LIMIT_PATH = "/mads/rate-limit/resource";

    // Static initializer runs before Spring context creation, ensuring Redis is up
    // before Spring's RedisRateLimiter attempts to connect.
    private static final int REDIS_PORT;
    private static final RedisServer REDIS_SERVER;

    static
    {
        try (ServerSocket socket = new ServerSocket(0))
        {
            REDIS_PORT = socket.getLocalPort();
        }
        catch (IOException e)
        {
            throw new ExceptionInInitializerError(e);
        }
        try
        {
            REDIS_SERVER = new RedisServer(REDIS_PORT);
            REDIS_SERVER.start();
        }
        catch (IOException e)
        {
            throw new ExceptionInInitializerError(e);
        }
    }

    @DynamicPropertySource
    static void redisProperties(DynamicPropertyRegistry registry)
    {
        registry.add("spring.data.redis.host", () -> "127.0.0.1");
        registry.add("spring.data.redis.port", () -> REDIS_PORT);
    }

    @AfterAll
    static void stopRedis() throws IOException
    {
        REDIS_SERVER.stop();
    }

    @Autowired
    private WebTestClient client;

    @BeforeEach
    void setUp()
    {
        stubFor(get(urlMatching("/mads/rate-limit/.*"))
            .willReturn(aResponse()
                .withStatus(200)
                .withBody("OK")
                .withHeader("Content-Type", "text/plain")));
    }

    @Test
    void requestsWithinBurstCapacity_shouldSucceed()
    {
        // burstCapacity=2: both requests get tokens and are forwarded
        client.get().uri(RATE_LIMIT_PATH)
              .header("X-API-KEY", "within-limit-key")
              .exchange()
              .expectStatus().isOk();

        client.get().uri(RATE_LIMIT_PATH)
              .header("X-API-KEY", "within-limit-key")
              .exchange()
              .expectStatus().isOk();
    }

    @Test
    void requestsExceedingBurstCapacity_shouldReturnTooManyRequests()
    {
        // Exhaust the 2-token burst bucket for this key
        client.get().uri(RATE_LIMIT_PATH).header("X-API-KEY", "over-limit-key").exchange();
        client.get().uri(RATE_LIMIT_PATH).header("X-API-KEY", "over-limit-key").exchange();

        // Bucket is empty — next request must be rejected
        client.get().uri(RATE_LIMIT_PATH)
              .header("X-API-KEY", "over-limit-key")
              .exchange()
              .expectStatus().isEqualTo(HttpStatus.TOO_MANY_REQUESTS);
    }

    @Test
    void differentApiKeys_haveIndependentRateLimitBuckets()
    {
        // Exhaust key-A's bucket
        client.get().uri(RATE_LIMIT_PATH).header("X-API-KEY", "key-A-independent").exchange();
        client.get().uri(RATE_LIMIT_PATH).header("X-API-KEY", "key-A-independent").exchange();

        client.get().uri(RATE_LIMIT_PATH)
              .header("X-API-KEY", "key-A-independent")
              .exchange()
              .expectStatus().isEqualTo(HttpStatus.TOO_MANY_REQUESTS);

        // key-B has its own full bucket — must not be affected by key-A being throttled
        client.get().uri(RATE_LIMIT_PATH)
              .header("X-API-KEY", "key-B-independent")
              .exchange()
              .expectStatus().isOk();
    }

    @Test
    void requestsWithoutApiKey_shareAnonymousBucketAndGetThrottled()
    {
        // All requests without X-API-KEY resolve to the "anonymous" key
        client.get().uri(RATE_LIMIT_PATH).exchange();
        client.get().uri(RATE_LIMIT_PATH).exchange();
        client.get().uri(RATE_LIMIT_PATH).exchange();
        client.get().uri(RATE_LIMIT_PATH).exchange();
        client.get().uri(RATE_LIMIT_PATH).exchange();
        client.get().uri(RATE_LIMIT_PATH).exchange();
        client.get().uri(RATE_LIMIT_PATH).exchange();
        client.get().uri(RATE_LIMIT_PATH).exchange();

        // Anonymous bucket is now empty
        client.get().uri(RATE_LIMIT_PATH)
              .exchange()
              .expectStatus().isEqualTo(HttpStatus.TOO_MANY_REQUESTS);
    }

    @Test
    void highTrafficBurst_returns429ForExcessRequests()
    {
        String apiKey = "high-traffic-key";
        List<Integer> statusCodes = new ArrayList<>();

        // Fire 5 rapid requests — first 2 should succeed, remaining 3 should be throttled
        for (int i = 0; i < 5; i++)
        {
            int status = client.get()
                               .uri(RATE_LIMIT_PATH)
                               .header("X-API-KEY", apiKey)
                               .exchange()
                               .returnResult(String.class)
                               .getStatus()
                               .value();
            statusCodes.add(status);
        }

        assertThat(statusCodes).contains(429)
                               .as("At least some requests must be rate-limited under high traffic");
        assertThat(statusCodes.stream().filter(s -> s == 200).count())
            .as("First requests within burst capacity must succeed")
            .isGreaterThanOrEqualTo(2);
    }

    @Test
    void rateLimitResponseHeaders_arePresent()
    {
        // Spring Cloud Gateway's RedisRateLimiter sets X-RateLimit-* headers on every response
        client.get().uri(RATE_LIMIT_PATH)
              .header("X-API-KEY", "header-check-key")
              .exchange()
              .expectHeader().exists("X-RateLimit-Remaining")
              .expectHeader().exists("X-RateLimit-Requested-Tokens");
    }
}
