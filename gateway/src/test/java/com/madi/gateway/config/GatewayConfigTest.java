package com.madi.gateway.config;


import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.wiremock.spring.EnableWireMock;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@EnableWireMock
@SpringBootTest
@ActiveProfiles("test")
class GatewayConfigTest
{
    @Autowired
    protected GatewayConfig config;

    @Test
    void defaults()
    {
        assertEquals(List.of(".pdf"), config.getExtensions());
        assertEquals("/home", config.getRedirectLoginPath());
        assertNotNull(config.getJwt().getSecret());
    }
}
