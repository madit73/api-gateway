package com.madi.common.utility.auth.jwt;


import com.madi.common.utility.auth.constant.GlobalAuthConstants;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import javax.crypto.SecretKey;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

/**
 * Helper for parsing and validating JWTs, with convenience accessors for common claims.
 */
@Slf4j
@Getter
public class JwtHelper
{
    public static final String ACCESS_KEY = "access";
    public static final String FIRST_NAME_KEY = "firstName";
    public static final String LAST_NAME_KEY = "lastName";
    public static final String EMAIL_KEY = "email";
    public static final String SITE_CODE_KEY = "siteCode";
    public static final String JWT_VERSION_KEY = "v";

    private Claims claims;
    private boolean isValid;

    private JwtHelper(String secret, String jwt)
    {
        try
        {
            if (!StringUtils.hasText(jwt))
            {
                log.info("JWT token is null or empty");
                isValid = false;
                claims = Jwts.claims().build();
            }
            else
            {
                if (jwt.startsWith(GlobalAuthConstants.BEARER))
                {
                    jwt = jwt.replace(GlobalAuthConstants.BEARER, "");
                }
                this.claims = getJwtClaims(secret, jwt);
                isValid = true;
            }
        }
        catch (ExpiredJwtException exp)
        {
            log.info("JWT token is expired");
            isValid = false;
            claims = exp.getClaims();
        }
        catch (Exception e)
        {
            isValid = false;
            claims = Jwts.claims().build();
            log.info("Unable to validate JWT token", e);
        }
    }

    /**
     * Builds an HMAC signing key from a base64-encoded secret.
     */
    public static SecretKey getSigningKey(String secret)
    {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
    }

    /**
     * Parses and validates a JWT, returning its claims when the signature is valid.
     */
    public static Claims getJwtClaims(@NotNull String secret, @NotNull String jwt)
    {
        return Jwts.parser().verifyWith(getSigningKey(secret)).build()
                   .parseSignedClaims(jwt.replace(GlobalAuthConstants.BEARER, "")).getPayload();
    }

    /**
     * Creates a helper using a raw (non-base64) secret, encoding it before parsing.
     */
    public static JwtHelper buildJwtHelperWithUnCodedSecret(@NotBlank String secret, @NotBlank String jwt)
    {
        String encodedSecret = Base64.getEncoder().encodeToString(secret.getBytes(StandardCharsets.UTF_8));
        return new JwtHelper(encodedSecret, jwt);
    }

    /**
     * Creates a helper using a base64-encoded secret.
     */
    public static JwtHelper buildJwtHelperWithEnCodedSecret(@NotBlank String secret, @NotBlank String jwt)
    {
        return new JwtHelper(secret, jwt);
    }

    public String getFirstName()
    {
        return getClaimData(FIRST_NAME_KEY, String.class);
    }

    public String getLastName()
    {
        return getClaimData(LAST_NAME_KEY, String.class);
    }

    public String getEmail()
    {
        return getClaimData(EMAIL_KEY, String.class);
    }

    public String getSiteCode()
    {
        return getClaimData(SITE_CODE_KEY, String.class);
    }

    public String getVersion()
    {
        return getClaimData(JWT_VERSION_KEY, String.class);
    }

    /**
     * Reads the access/privileges claim as a list of strings.
     */
    public List<String> getPrivileges()
    {
        return getClaimData(ACCESS_KEY, List.class);
    }

    /**
     * Reads a claim value and casts it to the expected type.
     */
    public <T> T getClaimData(@NotNull String claimName, Class<T> expectedResultType)
    {
        return getClaims().get(claimName, expectedResultType);
    }
}
