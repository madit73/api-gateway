package com.madi.gateway.config;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

/**
 * Externalized configuration for gateway behavior and JWT settings.
 */
@Slf4j
@Getter
@Setter
@Validated
@RefreshScope
@Component
@ConfigurationProperties("limitless.gw")
public class GatewayConfig
{
    @NotNull
    private List<String> extensions;

    @NotBlank
    private String redirectLoginPath;

    @NotNull
    private Jwt jwt = new Jwt();

    /**
     * JWT configuration values sourced from application properties.
     */
    @Getter
    @Setter
    @Validated
    @NoArgsConstructor
    public static class Jwt
    {
        private long tokenValidityInSeconds = 1200L;

        @NotBlank
        private String secret;
    }
}
