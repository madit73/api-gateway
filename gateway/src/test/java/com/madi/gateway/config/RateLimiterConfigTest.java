package com.madi.gateway.config;


import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import reactor.test.StepVerifier;

class RateLimiterConfigTest
{
    private final KeyResolver resolver = new RateLimiterConfig().apiKeyResolver();

    @Test
    void resolve_withApiKeyHeader_returnsHeaderValue()
    {
        var exchange = MockServerWebExchange.from(
            MockServerHttpRequest.get("/").header("X-API-KEY", "client-abc").build()
        );

        StepVerifier.create(resolver.resolve(exchange))
                    .expectNext("client-abc")
                    .verifyComplete();
    }

    @Test
    void resolve_withoutApiKeyHeader_returnsAnonymous()
    {
        var exchange = MockServerWebExchange.from(
            MockServerHttpRequest.get("/").build()
        );

        StepVerifier.create(resolver.resolve(exchange))
                    .expectNext("anonymous")
                    .verifyComplete();
    }

    @Test
    void resolve_withEmptyApiKeyHeader_returnsEmptyStringNotAnonymous()
    {
        // Empty string is not null — Mono.justOrEmpty emits it, defaultIfEmpty does not apply
        var exchange = MockServerWebExchange.from(
            MockServerHttpRequest.get("/").header("X-API-KEY", "").build()
        );

        StepVerifier.create(resolver.resolve(exchange))
                    .expectNext("")
                    .verifyComplete();
    }

    @Test
    void resolve_differentKeys_returnDistinctValues()
    {
        var exchange1 = MockServerWebExchange.from(
            MockServerHttpRequest.get("/").header("X-API-KEY", "key-one").build()
        );
        var exchange2 = MockServerWebExchange.from(
            MockServerHttpRequest.get("/").header("X-API-KEY", "key-two").build()
        );

        StepVerifier.create(resolver.resolve(exchange1)).expectNext("key-one").verifyComplete();
        StepVerifier.create(resolver.resolve(exchange2)).expectNext("key-two").verifyComplete();
    }

    @Test
    void resolve_withSpecialCharactersInApiKey_returnsValueAsIs()
    {
        var exchange = MockServerWebExchange.from(
            MockServerHttpRequest.get("/").header("X-API-KEY", "sk-prod_123.abc").build()
        );

        StepVerifier.create(resolver.resolve(exchange))
                    .expectNext("sk-prod_123.abc")
                    .verifyComplete();
    }
}
