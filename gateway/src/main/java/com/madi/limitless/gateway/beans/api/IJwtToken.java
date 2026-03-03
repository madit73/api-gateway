package com.madi.limitless.gateway.beans.api;


import org.springframework.web.server.ServerWebExchange;

public interface IJwtToken
{
    String getJwtToken(ServerWebExchange exchange);

    String refreshJwtToken(String oldJwt);
}