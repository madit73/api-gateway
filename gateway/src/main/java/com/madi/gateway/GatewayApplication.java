package com.madi.gateway;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

/**
 * Spring Boot entry point for the API gateway.
 */
@SpringBootApplication
public class GatewayApplication
{
    /**
     * Launches the gateway application.
     */
    public static void main(String[] args)
    {
        SpringApplication.run(GatewayApplication.class, args);
    }

    /**
     * Defines gateway routes (currently empty, configured elsewhere).
     */
    @Bean
    public RouteLocator myRoutes(RouteLocatorBuilder builder)
    {
        return builder.routes().build();
    }

    /**
     * Configures a permissive security chain for the gateway.
     */
    @Bean
    public SecurityWebFilterChain springWebFilterChain(ServerHttpSecurity http)
    {
        return http.csrf(ServerHttpSecurity.CsrfSpec::disable)
                   .authorizeExchange(authorizeExchangeSpec ->
                       authorizeExchangeSpec
                           .anyExchange().permitAll()
                   )
                   .build();
    }
}
