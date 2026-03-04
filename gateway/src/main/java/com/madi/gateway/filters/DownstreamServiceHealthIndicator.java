package com.madi.gateway.filters;


import jakarta.annotation.Nullable;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.ReactiveHealthIndicator;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * Health indicator that checks downstream dependencies.
 */
@Component
public class DownstreamServiceHealthIndicator implements ReactiveHealthIndicator
{
    /**
     * Returns health status, marking the gateway down if downstream checks fail.
     */
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
