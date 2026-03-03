package com.madi.limitless.gateway.beans.impl;


import com.madi.limitless.common.utility.auth.constant.GlobalAuthConstants;
import com.madi.limitless.common.utility.auth.jwt.JwtHelper;
import com.madi.limitless.gateway.beans.api.IJwtToken;
import com.madi.limitless.gateway.config.GatewayConfig;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpCookie;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.server.ServerWebExchange;

import static com.madi.limitless.common.utility.auth.jwt.JwtHelper.ACCESS_KEY;
import static com.madi.limitless.common.utility.auth.jwt.JwtHelper.EMAIL_KEY;
import static com.madi.limitless.common.utility.auth.jwt.JwtHelper.FIRST_NAME_KEY;
import static com.madi.limitless.common.utility.auth.jwt.JwtHelper.JWT_VERSION_KEY;
import static com.madi.limitless.common.utility.auth.jwt.JwtHelper.LAST_NAME_KEY;

/**
 * Retrieves and refreshes JWTs for gateway requests.
 */
@Slf4j
@Component
@Validated
@RequiredArgsConstructor
public class JwtToken implements IJwtToken
{
    private final GatewayConfig config;

    private String encodedSecret;

    private long tokenValidityInMilliseconds;

    /**
     * Precomputes the encoded signing key and token validity window.
     */
    @PostConstruct
    public void init()
    {
        this.encodedSecret = Base64.getEncoder().encodeToString(
            config.getJwt().getSecret().getBytes(StandardCharsets.UTF_8));
        this.tokenValidityInMilliseconds = config.getJwt().getTokenValidityInSeconds() * 1000L;
    }

    /**
     * Resolves the bearer token from either the auth cookie or authorization header.
     */
    public String getJwtToken(ServerWebExchange exchange)
    {
        ServerHttpRequest request = exchange.getRequest();
        String bearerStr = null;
        HttpCookie cookie = request.getCookies().getFirst(GlobalAuthConstants.AUTHORIZATION_HEADER);

        if (cookie != null && StringUtils.hasText(cookie.getValue()))
        {
            log.debug("{} Authorization token found in cookie", exchange.getLogPrefix());
            bearerStr = GlobalAuthConstants.BEARER + cookie.getValue();
        }
        else
        {
            List<String> values = request.getHeaders().get(GlobalAuthConstants.AUTHORIZATION_HEADER);
            if (!CollectionUtils.isEmpty(values))
            {
                for (String value : values)
                {
                    if (value.startsWith(GlobalAuthConstants.BEARER))
                    {
                        bearerStr = value;
                        break;
                    }
                }
            }

            if (!StringUtils.hasText(bearerStr))
            {
                log.debug("Authorization not found in header.");
            }
            else
            {
                log.debug(
                    "{} Authorization found in {} header", exchange.getLogPrefix(),
                    GlobalAuthConstants.AUTHORIZATION_HEADER
                );
            }
        }

        if (log.isDebugEnabled() && !StringUtils.hasText(bearerStr))
        {
            log.debug("{} No Authorization token found on request", exchange.getLogPrefix());
        }

        return bearerStr;
    }

    /**
     * Refreshes a JWT by copying claims and extending expiration.
     */
    @Override
    public String refreshJwtToken(String oldJwt)
    {
        Claims claims = JwtHelper.getJwtClaims(encodedSecret, oldJwt);

        long now = (new Date()).getTime();
        Date validity = new Date(now + this.tokenValidityInMilliseconds);

        return Jwts.builder().subject(claims.getSubject())
                   .claim(ACCESS_KEY, claims.get(ACCESS_KEY, ArrayList.class))
                   .claim(FIRST_NAME_KEY, claims.get(FIRST_NAME_KEY, String.class))
                   .claim(LAST_NAME_KEY, claims.get(LAST_NAME_KEY, String.class))
                   .claim(EMAIL_KEY, claims.get(EMAIL_KEY, String.class))
                   .claim(JWT_VERSION_KEY, GlobalAuthConstants.JWT_VERSION_VALUE)
                   .signWith(JwtHelper.getSigningKey(encodedSecret), Jwts.SIG.HS512)
                   .expiration(validity)
                   .compact();
    }
}
