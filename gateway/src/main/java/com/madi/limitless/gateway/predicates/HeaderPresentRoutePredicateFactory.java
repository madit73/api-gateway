package com.madi.limitless.gateway.predicates;


import jakarta.validation.constraints.NotBlank;
import java.util.function.Predicate;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.handler.predicate.AbstractRoutePredicateFactory;
import org.springframework.cloud.gateway.handler.predicate.GatewayPredicate;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.server.ServerWebExchange;

/**
 * Route predicate that matches based on presence (or absence) of a header.
 */
@Slf4j
@Component
@Validated
public class HeaderPresentRoutePredicateFactory
    extends AbstractRoutePredicateFactory<HeaderPresentRoutePredicateFactory.Config>
{
    public HeaderPresentRoutePredicateFactory()
    {
        super(Config.class);
    }

    /**
     * Builds a predicate that checks for the configured header name.
     */
    @Override
    public Predicate<ServerWebExchange> apply(Config config)
    {
        return new GatewayPredicate()
        {
            @Override
            public boolean test(ServerWebExchange exchange)
            {
                log.debug("{} {}", exchange.getLogPrefix(), this);
                String header = exchange.getRequest().getHeaders().getFirst(config.getName());
                if (config.isNegate())
                {
                    return header == null;
                }

                return header != null;
            }

            @Override
            public Object getConfig()
            {
                return config;
            }

            @Override
            public String toString()
            {
                return String.format("Header: %s", config.getName());
            }
        };
    }

    /**
     * Configuration for the header predicate.
     */
    @Getter
    @Setter
    @Validated
    @NoArgsConstructor
    public static class Config
    {
        @NotBlank
        private String name;

        private boolean negate = false;
    }
}
