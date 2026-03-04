package com.madi.gateway.filters;


import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.cloud.gateway.filter.RouteToRequestUrlFilter;
import org.springframework.core.Ordered;

/**
 * Ordered positions for gateway filters to ensure predictable execution.
 */
@Getter
@AllArgsConstructor
public enum FilterOrder
{
    /** Runs first to log routing decisions. */
    ROUTER_LOG(Ordered.HIGHEST_PRECEDENCE),
    /** Runs after route-to-URL to preserve target URI. */
    GLOBAL_URI(RouteToRequestUrlFilter.ROUTE_TO_URL_FILTER_ORDER + 1),
    /** Runs before downstream routing to enforce authorization. */
    AUTHORIZATION(-1);

    private final int order;
}
