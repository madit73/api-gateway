package com.madi.limitless.common.utility.auth.enums;


/**
 * Lifecycle status of a user account.
 */
public enum UserStatus
{
    /** Account created but not yet activated. */
    PENDING,
    /** Account is active and can authenticate. */
    ACTIVE,
    /** Account is inactive and should not authenticate. */
    INACTIVE,
    /** Account has been permanently removed or archived. */
    PURGED
}
