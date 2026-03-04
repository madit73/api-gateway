package com.madi.gateway.beans.api;


import org.springframework.web.server.ServerWebExchange;

/**
 * Accessor and refresh contract for JWTs in gateway requests.
 */
public interface IJwtToken
{
    /**
     * Extracts a bearer token from the incoming request.
     */
    String getJwtToken(ServerWebExchange exchange);

    /**
     * Creates a refreshed JWT based on the provided token.
     */
    String refreshJwtToken(String oldJwt);
}
