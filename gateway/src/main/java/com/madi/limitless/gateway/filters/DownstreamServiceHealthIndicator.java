package com.madi.limitless.gateway.filters;


import jakarta.annotation.Nullable;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.ReactiveHealthIndicator;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class DownstreamServiceHealthIndicator implements ReactiveHealthIndicator
{
    @Override
    @Nullable
    public Mono<Health> health()
    {
        return checkDownstreamServiceHealth()
            .onErrorResume(
                ex -> Mono.just(new Health.Builder().down(ex).build())
            );
    }

    private Mono<Health> checkDownstreamServiceHealth()
    {
        return Mono.just(new Health.Builder().up().build());
    }
}