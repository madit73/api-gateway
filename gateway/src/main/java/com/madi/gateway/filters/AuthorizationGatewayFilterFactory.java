package com.madi.gateway.filters;


import com.madi.limitless.common.utility.auth.constant.GlobalAuthConstants;
import com.madi.limitless.common.utility.auth.jwt.JwtHelper;
import com.madi.gateway.beans.impl.JwtToken;
import com.madi.gateway.config.GatewayConfig;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.OrderedGatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Gateway filter that validates JWTs and refreshes them on each request.
 */
@Slf4j
@Validated
@Component
public class AuthorizationGatewayFilterFactory
    extends AbstractGatewayFilterFactory<AuthorizationGatewayFilterFactory.Config>
{
    private final GatewayConfig gatewayConfig;
    private final JwtToken jwtToken;

    public AuthorizationGatewayFilterFactory(GatewayConfig gatewayConfig, JwtToken jwtToken)
    {
        super(AuthorizationGatewayFilterFactory.Config.class);
        this.gatewayConfig = gatewayConfig;
        this.jwtToken = jwtToken;
    }

    /**
     * Applies authentication checks and refresh logic to gateway requests.
     */
    @Override
    public GatewayFilter apply(AuthorizationGatewayFilterFactory.Config config)
    {
        return new OrderedGatewayFilter(
            (exchange, chain) -> {
                log.debug("{} Entering {}", exchange.getLogPrefix(), getClass().getSimpleName());

                boolean isValidExtension = config.getExtensions()
                                                 .stream()
                                                 .anyMatch(
                                                     ext -> exchange.getRequest().getPath().value().endsWith(ext));

                if (isRequestPublic(exchange, config))
                {
                    log.debug(
                        "{} Leaving {}.  Public endpoint, no authorization check needed.", exchange.getLogPrefix(),
                        getClass().getSimpleName()
                    );
                    return chain.filter(exchange);
                }

                String token = jwtToken.getJwtToken(exchange);

                JwtHelper jwtHelper = JwtHelper.buildJwtHelperWithUnCodedSecret(
                    gatewayConfig.getJwt().getSecret(), token);
                if (!StringUtils.hasText(token) && isValidExtension)
                {
                    log.debug(
                        "{} No authorization found but is a valid extension, so continuing with request",
                        exchange.getLogPrefix()
                    );
                    return chain.filter(exchange);
                }

                if (!StringUtils.hasText(token) || !jwtHelper.isValid())
                {
                    ServerHttpResponse response = exchange.getResponse();
                    // So the reason we are doing this is because when the front end is available & are on the same domain
                    // we want to be able to block ui requests to unauthorized pages as well
                    // as well as we could redirect to their login page
                    // todo see if i can just add the '/' in the config for prefixes. not sure if somewhere else wants it without yet
                    if (config.getPrefixes()
                              .stream()
                              .anyMatch(prefix -> exchange.getRequest().getPath().value().contains
                                  (prefix + "/")))
                    {
                        log.info(
                            "{} return 403 due to invalid JWT and application prefix found (call to a backend microservice).",
                            exchange.getLogPrefix()
                        );
                        response.setStatusCode(HttpStatus.FORBIDDEN);
                    }
                    else
                    {
                        log.info("{} Redirecting to login page due to invalid JWT.", exchange.getLogPrefix());
                        redirectUserToLogin(exchange, config);
                        response.setStatusCode(HttpStatus.TEMPORARY_REDIRECT);
                    }
                    logLeavingFunction(exchange);
                    return response.setComplete();
                }
                else
                {
                    //todo session changes can be made here, convert to if else if wanted
                    String newJwt = jwtToken.refreshJwtToken(
                        StringUtils.replace(token, GlobalAuthConstants.BEARER, ""));
                    ServerHttpRequest newRequest = updateRequest(exchange, newJwt);
                    logLeavingFunction(exchange);
                    return chain.filter(exchange.mutate().request(newRequest).build())
                                .then(Mono.fromRunnable(() -> setResponseCookieAndHeader(exchange, newJwt, config)));
                }
            }, FilterOrder.AUTHORIZATION.getOrder()
        );
    }

    /**
     * Adds the refreshed token to the outgoing request headers.
     */
    private ServerHttpRequest updateRequest(ServerWebExchange exchange, String newJwt)
    {
        ServerHttpRequest request = exchange.getRequest();
        log.debug(
            "{} Setting request {} header with refreshed token", exchange.getLogPrefix(),
            GlobalAuthConstants.AUTHORIZATION_HEADER
        );

        return request.mutate()
                      .headers(httpHeaders ->
                          httpHeaders.set(
                              GlobalAuthConstants.AUTHORIZATION_HEADER,
                              GlobalAuthConstants.BEARER + newJwt
                          )
                      )
                      .build();
    }

    /**
     * Determines if the current request matches a public endpoint.
     */
    private boolean isRequestPublic(ServerWebExchange exchange, Config config)
    {
        String rawPath = exchange.getRequest().getURI().getRawPath();

        if (!StringUtils.hasText(rawPath))
        {
            return false;
        }

        for (String pubEndpoints : config.getPublicEndpoints())
        {
            if (rawPath.contains(pubEndpoints))
            {
                log.debug("{} Public endpoint {}. Not checking JWT.", exchange.getLogPrefix(), pubEndpoints);
                return true;
            }
        }
        return false;
    }

    /**
     * Writes the refreshed token to both response header and cookie.
     */
    private void setResponseCookieAndHeader(
        ServerWebExchange exchange,
        String jwt,
        Config config
    )
    {
        log.debug(
            "{} Setting response {} header with token", exchange.getLogPrefix(),
            GlobalAuthConstants.AUTHORIZATION_HEADER
        );

        exchange.getResponse().getHeaders()
                .set(GlobalAuthConstants.AUTHORIZATION_HEADER, GlobalAuthConstants.BEARER + jwt);

        log.debug(
            "{} Setting response {} cookie with token", exchange.getLogPrefix(),
            GlobalAuthConstants.AUTHORIZATION_HEADER
        );

        exchange.getResponse().addCookie(createCookie(
            exchange,
            jwt.replace(GlobalAuthConstants.BEARER, ""),
            config.getCookieMaxAge()
        ));
    }

    /**
     * Builds a secure, HTTP-only cookie scoped to the gateway's context path.
     */
    private ResponseCookie createCookie(
        ServerWebExchange exchange,
        String value,
        long maxAge
    )
    {
        ServerHttpRequest request = exchange.getRequest();
        String cookiePath = request.getHeaders().getFirst(GlobalAuthConstants.X_FORWARDED_PREFIX);
        // might have to do: CommonUtil.sanitizeNoElementContent(cookiePath)
        cookiePath = (cookiePath == null) ? request.getPath().contextPath().value() : cookiePath;
        log.debug("{} Cookie path is: {} ", exchange.getLogPrefix(), cookiePath);

        return ResponseCookie.from(GlobalAuthConstants.AUTHORIZATION_HEADER, value)
                             .httpOnly(true)
                             .secure(true)
                             .path(cookiePath + (cookiePath.endsWith("/") ? "" : "/"))
                             .maxAge(maxAge)
                             .build();
    }

    /// this can be used with any front end
    /**
     * Issues a redirect to the configured login page.
     */
    private void redirectUserToLogin(ServerWebExchange exchange, Config config)
    {
        exchange.getResponse().getHeaders().add(GlobalAuthConstants.LOCATION_HEADER, config.getTimeoutUrl());
        log.debug("{} Setting response to temporary redirect to {}", exchange.getLogPrefix(), config.getTimeoutUrl());
    }

    private void logLeavingFunction(ServerWebExchange exchange)
    {
        log.debug("{} Leaving {}", exchange.getLogPrefix(), getClass().getSimpleName());
    }

    /**
     * Configuration properties for the authorization filter.
     */
    @Getter
    @Setter
    @Validated
    @NoArgsConstructor
    public static class Config
    {
        @NotEmpty
        private List<String> prefixes = List.of("/portal");

        @NotBlank
        private String timeoutUrl;

        private long cookieMaxAge = 1200; // age in seconds
        @NotEmpty
        private List<String> extensions = Collections.singletonList(
            ".woff2,.css,.scss,.ico,.xml,.map,.json,.css,.img," +
                ".js,.partials,.rest,.favicon,.jpg,.jpeg,.gif,.png,.svg,.html,.pdf,.axd");

        private Set<String> publicEndpoints = new HashSet<>(Arrays.asList(
            "/authenticate",
            "/signout",
            "/swagger",
            "/api-docs"
        ));
    }
}
