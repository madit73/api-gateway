package com.madi.limitless.common.utility.auth.jwt;


import com.madi.limitless.common.utility.auth.constant.GlobalAuthConstants;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Map;
import javax.crypto.SecretKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("JwtHelper Tests")
class JwtHelperTest
{

    private static final String TEST_SECRET = "testSecretKeyThatIsLongEnoughForHS256AlgorithmToWorkProperly";
    private static final String ENCODED_SECRET = Base64.getEncoder()
                                                       .encodeToString(TEST_SECRET.getBytes(StandardCharsets.UTF_8));

    private static final String TEST_FIRST_NAME = "John";
    private static final String TEST_LAST_NAME = "Doe";
    private static final String TEST_EMAIL = "john.doe@example.com";
    private static final String TEST_SITE_CODE = "SITE1";
    private static final String TEST_VERSION = "1.0";
    private static final List<String> TEST_PRIVILEGES = List.of("READ", "WRITE", "DELETE");

    private String validJwt;
    private String expiredJwt;

    @BeforeEach
    void setUp()
    {
        validJwt = createTestJwt(ENCODED_SECRET, false);
        expiredJwt = createTestJwt(ENCODED_SECRET, true);
    }

    @Nested
    @DisplayName("Builder Methods Tests")
    class BuilderMethodsTests
    {

        @Test
        @DisplayName("Should build JwtHelper with uncoded secret successfully")
        void shouldBuildJwtHelperWithUncodedSecret()
        {
            JwtHelper helper = JwtHelper.buildJwtHelperWithUnCodedSecret(TEST_SECRET, validJwt);

            assertNotNull(helper);
            assertTrue(helper.isValid());
            assertNotNull(helper.getClaims());
        }

        @Test
        @DisplayName("Should build JwtHelper with encoded secret successfully")
        void shouldBuildJwtHelperWithEnCodedSecret()
        {
            JwtHelper helper = JwtHelper.buildJwtHelperWithEnCodedSecret(ENCODED_SECRET, validJwt);

            assertNotNull(helper);
            assertTrue(helper.isValid());
            assertNotNull(helper.getClaims());
        }

        @Test
        @DisplayName("Should handle JWT with Bearer prefix")
        void shouldHandleJwtWithBearerPrefix()
        {
            String jwtWithBearer = GlobalAuthConstants.BEARER + validJwt;
            JwtHelper helper = JwtHelper.buildJwtHelperWithEnCodedSecret(ENCODED_SECRET, jwtWithBearer);

            assertTrue(helper.isValid());
            assertEquals(TEST_EMAIL, helper.getEmail());
        }

        @Test
        @DisplayName("Should handle JWT without Bearer prefix")
        void shouldHandleJwtWithoutBearerPrefix()
        {
            JwtHelper helper = JwtHelper.buildJwtHelperWithEnCodedSecret(ENCODED_SECRET, validJwt);

            assertTrue(helper.isValid());
            assertEquals(TEST_EMAIL, helper.getEmail());
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = { "   ", "\t", "\n" })
        @DisplayName("Should mark as invalid when JWT is null, empty or blank")
        void shouldMarkAsInvalidWhenJwtIsNullOrEmpty(String jwt)
        {
            JwtHelper helper = JwtHelper.buildJwtHelperWithEnCodedSecret(ENCODED_SECRET, jwt);

            assertFalse(helper.isValid());
            assertNotNull(helper.getClaims());
        }

        @Test
        @DisplayName("Should handle expired JWT token")
        void shouldHandleExpiredJwt()
        {
            JwtHelper helper = JwtHelper.buildJwtHelperWithEnCodedSecret(ENCODED_SECRET, expiredJwt);

            assertFalse(helper.isValid());
            assertNotNull(helper.getClaims());
            // Even though expired, claims should still be accessible
            assertEquals(TEST_EMAIL, helper.getEmail());
        }

        @Test
        @DisplayName("Should mark as invalid when JWT is malformed")
        void shouldMarkAsInvalidWhenJwtIsMalformed()
        {
            String malformedJwt = "this.is.not.a.valid.jwt";
            JwtHelper helper = JwtHelper.buildJwtHelperWithEnCodedSecret(ENCODED_SECRET, malformedJwt);

            assertFalse(helper.isValid());
            assertNotNull(helper.getClaims());
        }

        @Test
        @DisplayName("Should mark as invalid when secret is incorrect")
        void shouldMarkAsInvalidWhenSecretIsIncorrect()
        {
            String wrongSecret = Base64.getEncoder()
                                       .encodeToString("wrongSecret123456789012345678901234567890".getBytes(
                                           StandardCharsets.UTF_8));
            JwtHelper helper = JwtHelper.buildJwtHelperWithEnCodedSecret(wrongSecret, validJwt);

            assertFalse(helper.isValid());
        }
    }

    @Nested
    @DisplayName("Claim Extraction Tests")
    class ClaimExtractionTests
    {

        private JwtHelper helper;

        @BeforeEach
        void setUp()
        {
            helper = JwtHelper.buildJwtHelperWithEnCodedSecret(ENCODED_SECRET, validJwt);
        }

        @Test
        @DisplayName("Should extract first name from claims")
        void shouldExtractFirstName()
        {
            assertEquals(TEST_FIRST_NAME, helper.getFirstName());
        }

        @Test
        @DisplayName("Should extract last name from claims")
        void shouldExtractLastName()
        {
            assertEquals(TEST_LAST_NAME, helper.getLastName());
        }

        @Test
        @DisplayName("Should extract email from claims")
        void shouldExtractEmail()
        {
            assertEquals(TEST_EMAIL, helper.getEmail());
        }

        @Test
        @DisplayName("Should extract site code from claims")
        void shouldExtractSiteCode()
        {
            assertEquals(TEST_SITE_CODE, helper.getSiteCode());
        }

        @Test
        @DisplayName("Should extract version from claims")
        void shouldExtractVersion()
        {
            assertEquals(TEST_VERSION, helper.getVersion());
        }

        @Test
        @DisplayName("Should extract privileges from claims")
        void shouldExtractPrivileges()
        {
            List<String> privileges = helper.getPrivileges();

            assertNotNull(privileges);
            assertEquals(TEST_PRIVILEGES.size(), privileges.size());
            assertTrue(privileges.containsAll(TEST_PRIVILEGES));
        }

        @Test
        @DisplayName("Should extract custom claim data with correct type")
        void shouldExtractCustomClaimData()
        {
            String customValue = helper.getClaimData("email", String.class);
            assertEquals(TEST_EMAIL, customValue);
        }

        @Test
        @DisplayName("Should return null for non-existent claim")
        void shouldReturnNullForNonExistentClaim()
        {
            String nonExistent = helper.getClaimData("nonExistentClaim", String.class);
            assertNull(nonExistent);
        }

        @Test
        @DisplayName("Should extract claims from expired JWT")
        void shouldExtractClaimsFromExpiredJwt()
        {
            JwtHelper expiredHelper = JwtHelper.buildJwtHelperWithEnCodedSecret(ENCODED_SECRET, expiredJwt);

            assertFalse(expiredHelper.isValid());
            assertEquals(TEST_EMAIL, expiredHelper.getEmail());
            assertEquals(TEST_FIRST_NAME, expiredHelper.getFirstName());
        }
    }

    @Nested
    @DisplayName("Static Method Tests")
    class StaticMethodTests
    {

        @Test
        @DisplayName("Should get signing key from secret")
        void shouldGetSigningKeyFromSecret()
        {
            SecretKey key = JwtHelper.getSigningKey(ENCODED_SECRET);

            assertNotNull(key);
            assertTrue(key.getAlgorithm().startsWith("HmacSHA"));
        }

        @Test
        @DisplayName("Should get JWT claims from valid token")
        void shouldGetJwtClaimsFromValidToken()
        {
            Claims claims = JwtHelper.getJwtClaims(ENCODED_SECRET, validJwt);

            assertNotNull(claims);
            assertEquals(TEST_EMAIL, claims.get(JwtHelper.EMAIL_KEY, String.class));
            assertEquals(TEST_FIRST_NAME, claims.get(JwtHelper.FIRST_NAME_KEY, String.class));
        }

        @Test
        @DisplayName("Should handle Bearer prefix in getJwtClaims")
        void shouldHandleBearerPrefixInGetJwtClaims()
        {
            String jwtWithBearer = GlobalAuthConstants.BEARER + validJwt;
            Claims claims = JwtHelper.getJwtClaims(ENCODED_SECRET, jwtWithBearer);

            assertNotNull(claims);
            assertEquals(TEST_EMAIL, claims.get(JwtHelper.EMAIL_KEY, String.class));
        }

        @Test
        @DisplayName("Should throw exception for expired token in getJwtClaims")
        void shouldThrowExceptionForExpiredTokenInGetJwtClaims()
        {
            assertThrows(
                ExpiredJwtException.class, () -> {
                    JwtHelper.getJwtClaims(ENCODED_SECRET, expiredJwt);
                }
            );
        }

        @Test
        @DisplayName("Should throw exception for malformed token in getJwtClaims")
        void shouldThrowExceptionForMalformedTokenInGetJwtClaims()
        {
            assertThrows(
                Exception.class, () -> {
                    JwtHelper.getJwtClaims(ENCODED_SECRET, "invalid.jwt.token");
                }
            );
        }

        @Test
        @DisplayName("Should throw exception for invalid secret in getSigningKey")
        void shouldThrowExceptionForInvalidSecretInGetSigningKey()
        {
            assertThrows(
                Exception.class, () -> {
                    JwtHelper.getSigningKey("not-base64-encoded");
                }
            );
        }
    }

    @Nested
    @DisplayName("Getter Tests")
    class GetterTests
    {

        @Test
        @DisplayName("Should get claims via getter")
        void shouldGetClaimsViaGetter()
        {
            JwtHelper helper = JwtHelper.buildJwtHelperWithEnCodedSecret(ENCODED_SECRET, validJwt);
            Claims claims = helper.getClaims();

            assertNotNull(claims);
            assertTrue(claims.containsKey(JwtHelper.EMAIL_KEY));
        }

        @Test
        @DisplayName("Should get isValid status via getter")
        void shouldGetIsValidViaGetter()
        {
            JwtHelper validHelper = JwtHelper.buildJwtHelperWithEnCodedSecret(ENCODED_SECRET, validJwt);
            assertTrue(validHelper.isValid());

            JwtHelper invalidHelper = JwtHelper.buildJwtHelperWithEnCodedSecret(ENCODED_SECRET, "invalid");
            assertFalse(invalidHelper.isValid());
        }
    }

    @Nested
    @DisplayName("Constant Value Tests")
    class ConstantValueTests
    {

        @Test
        @DisplayName("Should have correct constant values")
        void shouldHaveCorrectConstantValues()
        {
            assertEquals("access", JwtHelper.ACCESS_KEY);
            assertEquals("firstName", JwtHelper.FIRST_NAME_KEY);
            assertEquals("lastName", JwtHelper.LAST_NAME_KEY);
            assertEquals("email", JwtHelper.EMAIL_KEY);
            assertEquals("siteCode", JwtHelper.SITE_CODE_KEY);
            assertEquals("v", JwtHelper.JWT_VERSION_KEY);
        }
    }

    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCasesTests
    {

        @Test
        @DisplayName("Should handle JWT with only some claims present")
        void shouldHandleJwtWithPartialClaims()
        {
            String partialJwt = createPartialClaimsJwt(ENCODED_SECRET);
            JwtHelper helper = JwtHelper.buildJwtHelperWithEnCodedSecret(ENCODED_SECRET, partialJwt);

            assertTrue(helper.isValid());
            assertEquals(TEST_EMAIL, helper.getEmail());
            assertNull(helper.getFirstName());
            assertNull(helper.getLastName());
        }

        @Test
        @DisplayName("Should handle JWT with empty privilege list")
        void shouldHandleJwtWithEmptyPrivilegeList()
        {
            String emptyPrivilegesJwt = createEmptyPrivilegesJwt(ENCODED_SECRET);
            JwtHelper helper = JwtHelper.buildJwtHelperWithEnCodedSecret(ENCODED_SECRET, emptyPrivilegesJwt);

            assertTrue(helper.isValid());
            List<String> privileges = helper.getPrivileges();
            assertNotNull(privileges);
            assertTrue(privileges.isEmpty());
        }

        @Test
        @DisplayName("Should handle JWT with null privilege list")
        void shouldHandleJwtWithNullPrivilegeList()
        {
            String nullPrivilegesJwt = createNullPrivilegesJwt(ENCODED_SECRET);
            JwtHelper helper = JwtHelper.buildJwtHelperWithEnCodedSecret(ENCODED_SECRET, nullPrivilegesJwt);

            assertTrue(helper.isValid());
            assertNull(helper.getPrivileges());
        }

        @Test
        @DisplayName("Should handle multiple Bearer prefix removals")
        void shouldHandleMultipleBearerPrefixes()
        {
            // Edge case: what if someone accidentally adds Bearer twice
            String doubleBearerJwt = GlobalAuthConstants.BEARER + GlobalAuthConstants.BEARER + validJwt;
            JwtHelper helper = JwtHelper.buildJwtHelperWithEnCodedSecret(ENCODED_SECRET, doubleBearerJwt);

            // The code removes Bearer prefix, so this will succeed since both are removed
            // (String.replace replaces all occurrences)
            assertTrue(helper.isValid());
        }

        @Test
        @DisplayName("Should handle JWT signed with different algorithm")
        void shouldHandleJwtWithDifferentAlgorithm()
        {
            // Create JWT with different secret
            String differentSecret = Base64.getEncoder()
                                           .encodeToString("differentSecretKey1234567890123456789012".getBytes(
                                               StandardCharsets.UTF_8));
            String differentJwt = createTestJwt(differentSecret, false);

            // Try to validate with original secret
            JwtHelper helper = JwtHelper.buildJwtHelperWithEnCodedSecret(ENCODED_SECRET, differentJwt);

            assertFalse(helper.isValid());
        }
    }

    @Nested
    @DisplayName("Integration Tests")
    class IntegrationTests
    {

        @Test
        @DisplayName("Should handle complete workflow with uncoded secret")
        void shouldHandleCompleteWorkflowWithUncodedSecret()
        {
            String jwt = createTestJwt(ENCODED_SECRET, false);
            JwtHelper helper = JwtHelper.buildJwtHelperWithUnCodedSecret(TEST_SECRET, jwt);

            assertTrue(helper.isValid());
            assertEquals(TEST_FIRST_NAME, helper.getFirstName());
            assertEquals(TEST_LAST_NAME, helper.getLastName());
            assertEquals(TEST_EMAIL, helper.getEmail());
            assertEquals(TEST_SITE_CODE, helper.getSiteCode());
            assertEquals(TEST_VERSION, helper.getVersion());
            assertEquals(TEST_PRIVILEGES, helper.getPrivileges());
        }

        @Test
        @DisplayName("Should handle complete workflow with encoded secret")
        void shouldHandleCompleteWorkflowWithEncodedSecret()
        {
            String jwt = createTestJwt(ENCODED_SECRET, false);
            JwtHelper helper = JwtHelper.buildJwtHelperWithEnCodedSecret(ENCODED_SECRET, jwt);

            assertTrue(helper.isValid());
            assertEquals(TEST_FIRST_NAME, helper.getFirstName());
            assertEquals(TEST_LAST_NAME, helper.getLastName());
            assertEquals(TEST_EMAIL, helper.getEmail());
            assertEquals(TEST_SITE_CODE, helper.getSiteCode());
            assertEquals(TEST_VERSION, helper.getVersion());
            assertEquals(TEST_PRIVILEGES, helper.getPrivileges());
        }

        @Test
        @DisplayName("Should validate and extract claims in single operation")
        void shouldValidateAndExtractClaimsInSingleOperation()
        {
            JwtHelper helper = JwtHelper.buildJwtHelperWithEnCodedSecret(ENCODED_SECRET, validJwt);

            // Check validity and extract all claims
            if (helper.isValid())
            {
                assertAll(
                    "All claims should be extractable",
                    () -> assertNotNull(helper.getEmail()),
                    () -> assertNotNull(helper.getFirstName()),
                    () -> assertNotNull(helper.getLastName()),
                    () -> assertNotNull(helper.getSiteCode()),
                    () -> assertNotNull(helper.getVersion()),
                    () -> assertNotNull(helper.getPrivileges())
                );
            }
        }
    }

    // Helper methods to create test JWTs

    private String createTestJwt(String encodedSecret, boolean expired)
    {
        SecretKey key = Keys.hmacShaKeyFor(Base64.getDecoder().decode(encodedSecret));

        Instant now = Instant.now();
        Date issuedAt = Date.from(now);
        Date expiration = expired
                          ? Date.from(now.minus(1, ChronoUnit.HOURS))
                          : Date.from(now.plus(1, ChronoUnit.HOURS));

        return Jwts.builder()
                   .subject(TEST_EMAIL)
                   .claim(JwtHelper.FIRST_NAME_KEY, TEST_FIRST_NAME)
                   .claim(JwtHelper.LAST_NAME_KEY, TEST_LAST_NAME)
                   .claim(JwtHelper.EMAIL_KEY, TEST_EMAIL)
                   .claim(JwtHelper.SITE_CODE_KEY, TEST_SITE_CODE)
                   .claim(JwtHelper.JWT_VERSION_KEY, TEST_VERSION)
                   .claim(JwtHelper.ACCESS_KEY, TEST_PRIVILEGES)
                   .issuedAt(issuedAt)
                   .expiration(expiration)
                   .signWith(key)
                   .compact();
    }

    private String createPartialClaimsJwt(String encodedSecret)
    {
        SecretKey key = Keys.hmacShaKeyFor(Base64.getDecoder().decode(encodedSecret));

        Instant now = Instant.now();
        Date issuedAt = Date.from(now);
        Date expiration = Date.from(now.plus(1, ChronoUnit.HOURS));

        return Jwts.builder()
                   .subject(TEST_EMAIL)
                   .claim(JwtHelper.EMAIL_KEY, TEST_EMAIL)
                   .issuedAt(issuedAt)
                   .expiration(expiration)
                   .signWith(key)
                   .compact();
    }

    private String createEmptyPrivilegesJwt(String encodedSecret)
    {
        SecretKey key = Keys.hmacShaKeyFor(Base64.getDecoder().decode(encodedSecret));

        Instant now = Instant.now();
        Date issuedAt = Date.from(now);
        Date expiration = Date.from(now.plus(1, ChronoUnit.HOURS));

        return Jwts.builder()
                   .subject(TEST_EMAIL)
                   .claim(JwtHelper.EMAIL_KEY, TEST_EMAIL)
                   .claim(JwtHelper.ACCESS_KEY, List.of())
                   .issuedAt(issuedAt)
                   .expiration(expiration)
                   .signWith(key)
                   .compact();
    }

    private String createNullPrivilegesJwt(String encodedSecret)
    {
        SecretKey key = Keys.hmacShaKeyFor(Base64.getDecoder().decode(encodedSecret));

        Instant now = Instant.now();
        Date issuedAt = Date.from(now);
        Date expiration = Date.from(now.plus(1, ChronoUnit.HOURS));

        return Jwts.builder()
                   .subject(TEST_EMAIL)
                   .claim(JwtHelper.EMAIL_KEY, TEST_EMAIL)
                   .claims(Map.of()) // Don't add ACCESS_KEY at all
                   .issuedAt(issuedAt)
                   .expiration(expiration)
                   .signWith(key)
                   .compact();
    }
}
