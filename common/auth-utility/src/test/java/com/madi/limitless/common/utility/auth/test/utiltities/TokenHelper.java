package com.madi.limitless.common.utility.auth.test.utiltities;


import com.madi.limitless.common.utility.auth.jwt.JwtHelper;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import java.util.Date;
import java.util.List;

public interface TokenHelper
{
    static String createToken(long expirationOffset, String encodedSecret)
    {
        JwtBuilder builder
            = Jwts.builder().subject("test")
                  .claim(JwtHelper.ACCESS_KEY, List.of("TEST_PRIV1", "TEST_PRIV2"))
                  .claim(JwtHelper.FIRST_NAME_KEY, "Test")
                  .claim(JwtHelper.LAST_NAME_KEY, "Last")
                  .claim(JwtHelper.EMAIL_KEY, "test.last@noaa.gov")
                  .claim(JwtHelper.SITE_CODE_KEY, "LWX")
                  .claim(JwtHelper.JWT_VERSION_KEY, "1.0");

        long now = (new Date()).getTime();
        Date validity = new Date(now + (expirationOffset * 1000));

        return builder.signWith(JwtHelper.getSigningKey(encodedSecret), Jwts.SIG.HS512)
                      .expiration(validity).compact();
    }
}
