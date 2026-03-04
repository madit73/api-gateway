package com.madi.gateway.filters;


import com.madi.limitless.common.utility.auth.constant.GlobalAuthConstants;
import java.net.URI;
import java.util.Collections;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.GATEWAY_REQUEST_URL_ATTR;

/**
 * Logs gateway routing details, including original URI and target route.
 */
@Slf4j
@Component
@Validated
@RequiredArgsConstructor
public class RouterLogFilter implements GlobalFilter, Ordered
{
    /**
     * Adds correlation ID headers for specific routes and logs request/response metadata.
     */
    @Override
    public Mono<Void> filter(
        ServerWebExchange exchange,
        GatewayFilterChain chain
    )
    {
        log.debug("Start logging request info for exchange {}", exchange.getLogPrefix());
        if ("mads-portal".equalsIgnoreCase(
            getRouteName(exchange.getAttribute(ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR))))
        {
            ServerHttpRequest request = exchange.getRequest();
            ServerHttpRequest newRequest = request.mutate().headers(httpHeaders -> httpHeaders
                .set(GlobalAuthConstants.CORRELATION_ID, exchange.getLogPrefix())).build();

            return chain.filter(exchange.mutate().request(newRequest).build())
                        .then(Mono.fromRunnable(() -> printInfo(exchange)));
        }
        else
        {
            return chain.filter(exchange).then(Mono.fromRunnable(() -> printInfo(exchange)));
        }
    }

    private void printInfo(ServerWebExchange exchange)
    {
        Set<URI> uris = exchange.getAttributeOrDefault(
            ServerWebExchangeUtils.GATEWAY_ORIGINAL_REQUEST_URL_ATTR, Collections.emptySet());
        String originalUri = (uris.isEmpty())
                             ? exchange.getRequest().getURI().toASCIIString()
                             : uris.iterator().next().toASCIIString();
        Route route = exchange.getAttribute(ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR);
        URI routeUri = null;
        if (route != null)
        {
            if (exchange.getAttribute(GATEWAY_REQUEST_URL_ATTR) != null)
            {
                routeUri = exchange.getAttribute(GATEWAY_REQUEST_URL_ATTR);
            }
            else
            {
                routeUri = route.getUri();
            }
        }

        ServerHttpRequest request = exchange.getRequest();

        log.info(
            "{} Incoming {} request from {} is routed using {} and forwarded to <{}>",
            exchange.getLogPrefix(),
            request.getMethod(),
            originalUri,
            getRouteName(route),
            routeUri
        );
        printRequestHeaders(exchange);
        printResponseInfo(exchange);
        log.debug("End logging request info for exchange {}", exchange.getLogPrefix());
    }

    private String getRouteName(Route route)
    {
        if (route == null)
        {
            return "Unknown";
        }
        else
        {
            return route.getId();
        }
    }

    private void printRequestHeaders(ServerWebExchange exchange)
    {
        if (log.isDebugEnabled())
        {
            log.debug("{} Request headers: {}", exchange.getLogPrefix(), exchange.getRequest().getHeaders());
        }
    }

    private void printResponseInfo(ServerWebExchange exchange)
    {
        if (exchange.getResponse().getStatusCode() == null)
        {
            log.info("Response status unset");
        }
        else
        {
            log.info(
                "{} Response status {} ({})", exchange.getLogPrefix(),
                exchange.getResponse().getStatusCode(), exchange.getResponse().getStatusCode()
            );
        }
        if (log.isDebugEnabled())
        {
            log.debug("{} Response headers: {}", exchange.getLogPrefix(), exchange.getResponse().getHeaders());
        }
    }

    /**
     * Orders this filter ahead of standard route filters.
     */
    @Override
    public int getOrder()
    {
        return FilterOrder.ROUTER_LOG.getOrder();
    }
}
