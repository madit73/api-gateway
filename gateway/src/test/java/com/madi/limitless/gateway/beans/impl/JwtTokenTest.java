package com.madi.limitless.gateway.beans.impl;


import com.madi.limitless.common.utility.auth.constant.GlobalAuthConstants;
import com.madi.limitless.common.utility.auth.jwt.JwtHelper;
import com.madi.limitless.common.utility.auth.test.utiltities.TokenHelper;
import com.madi.limitless.gateway.config.GatewayConfig;
import io.jsonwebtoken.Claims;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.server.ServerWebExchange;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest
class JwtTokenTest implements TokenHelper
{
    // does jakarta not have an @inject annotation???
    @Autowired
    protected GatewayConfig config;

    @Autowired
    private JwtToken jwtToken;

    @MockitoBean
    private ServerWebExchange exchange;

    @MockitoBean
    private ServerHttpRequest request;

    private String encodedSecret;

    @BeforeEach
    public void setup()
    {
        encodedSecret = Base64.getEncoder()
                              .encodeToString(config.getJwt().getSecret().getBytes(StandardCharsets.UTF_8));

        when(exchange.getRequest()).thenReturn(request);
        when(request.getCookies()).thenReturn(new LinkedMultiValueMap<>());
        when(request.getHeaders()).thenReturn(new HttpHeaders());
    }

    @Test
    public void noJwt()
    {
        assertNull(jwtToken.getJwtToken(exchange));
    }

    @Test
    public void jwtInCookie()
    {
        String jwt = TokenHelper.createToken(50, encodedSecret);
        HttpCookie cookie = mock(HttpCookie.class);
        when(cookie.getValue()).thenReturn(jwt);
        MultiValueMap<String, HttpCookie> cookies = new LinkedMultiValueMap<>();
        cookies.add(GlobalAuthConstants.AUTHORIZATION_HEADER, cookie);
        when(request.getCookies()).thenReturn(cookies);

        String result = jwtToken.getJwtToken(exchange);
        assertEquals(GlobalAuthConstants.BEARER + jwt, result);
    }

    @Test
    public void jwtInAuthorizationHeader()
    {
        String jwt = TokenHelper.createToken(50, encodedSecret);
        HttpHeaders headers = new HttpHeaders();
        headers.add(GlobalAuthConstants.AUTHORIZATION_HEADER, GlobalAuthConstants.BEARER + jwt);
        when(request.getHeaders()).thenReturn(headers);
        String result = jwtToken.getJwtToken(exchange);
        assertEquals(GlobalAuthConstants.BEARER + jwt, result);
    }

    @Test
    public void refreshToken()
    {
        String oldToken = TokenHelper.createToken(50, encodedSecret);
        Claims oldClaims = JwtHelper.buildJwtHelperWithEnCodedSecret(encodedSecret, oldToken).getClaims();
        String newToken = jwtToken.refreshJwtToken(oldToken);
        Claims newClaims = JwtHelper.buildJwtHelperWithEnCodedSecret(encodedSecret, newToken).getClaims();
        assertTrue(newClaims.getExpiration().after(oldClaims.getExpiration()));
    }
}