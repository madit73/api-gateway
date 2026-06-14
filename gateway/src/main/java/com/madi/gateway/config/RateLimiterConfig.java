package com.madi.gateway.config;


import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;
import reactor.core.publisher.Mono;

@Slf4j
@Getter
@Setter
@Validated
@RefreshScope
@Component
@ConfigurationProperties("limitless.gw.headers")
public class RateLimiterConfig
{
    private String apiKeyHeader = "X-API-KEY";

    @Bean
    public KeyResolver apiKeyResolver()
    {
        return exchange ->
            Mono.justOrEmpty(exchange.getRequest().getHeaders().getFirst(apiKeyHeader))
                .defaultIfEmpty("anonymous");
    }
}
