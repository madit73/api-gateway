package com.madi.gateway.predicates;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.ServerWebExchange;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HeaderPresentRoutePredicateFactoryTest
{

    private HeaderPresentRoutePredicateFactory.Config config;

    @Mock
    private HttpHeaders httpHeaders;

    @Mock
    private ServerWebExchange exchange;

    @InjectMocks
    private HeaderPresentRoutePredicateFactory factory;

    @BeforeEach
    void setup()
    {
        config = new HeaderPresentRoutePredicateFactory.Config();
        config.setName("test");

        ServerHttpRequest request = mock(ServerHttpRequest.class);
        when(exchange.getRequest()).thenReturn(request);
        when(request.getHeaders()).thenReturn(httpHeaders);
    }

    @Test
    void headerPresent()
    {
        when(httpHeaders.getFirst("test")).thenReturn("test");
        assertTrue(factory.apply(config).test(exchange));
    }

    @Test
    void headerNotPresent()
    {
        when(httpHeaders.getFirst("test")).thenReturn(null);
        assertFalse(factory.apply(config).test(exchange));
    }
}