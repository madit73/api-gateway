package com.madi.common.utility.auth.constant;


import java.util.regex.Pattern;
import lombok.experimental.UtilityClass;

/**
 * Shared authentication constants and validation rules used across auth and gateway modules.
 */
@UtilityClass
public final class GlobalAuthConstants
{
    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String BEARER = "Bearer ";
    public static final String X_FORWARDED_PREFIX = "X-Forwarded-Prefix";
    public static final String LOCATION_HEADER = "Location";
    public static final String CORRELATION_ID = "limitless-correlation-id";
    public static final String JWT_VERSION_VALUE = "1.0";
    public static final String ANONYMOUS_USER = "anonymoususer";
    public static final String ROLE_ANONYMOUS = "ROLE_ANONYMOUS";

    // TODO make config?
    public static final int PASSWORD_MIN_LENGTH = 4;
    public static final int PASSWORD_MAX_LENGTH = 20;
    public static final String PHONE_REGEX_OPTIONAL = "^\\s*|\\(?([0-9]{3})\\)?[-.\\s]?([0-9]{3})[-.\\s]?([0-9]{4})$";
    public static final Pattern VALID_EMAIL_ADDRESS_REGEX =
        Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);
    // Regex for acceptable logins
    public static final String LOGIN_REGEX = "^[_.@+A-Za-z0-9-]*$";

    public static final int SITE_CODE_LENGTH = 5;
}
