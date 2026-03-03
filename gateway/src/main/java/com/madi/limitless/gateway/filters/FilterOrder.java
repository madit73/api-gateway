package com.madi.limitless.gateway.filters;


import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.cloud.gateway.filter.RouteToRequestUrlFilter;
import org.springframework.core.Ordered;

@Getter
@AllArgsConstructor
public enum FilterOrder
{
    ROUTER_LOG(Ordered.HIGHEST_PRECEDENCE),
    GLOBAL_URI(RouteToRequestUrlFilter.ROUTE_TO_URL_FILTER_ORDER + 1),
    AUTHORIZATION(-1);

    private final int order;
}
