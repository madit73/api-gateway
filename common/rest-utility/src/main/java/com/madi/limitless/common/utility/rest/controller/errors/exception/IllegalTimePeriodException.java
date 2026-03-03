package com.madi.limitless.common.utility.rest.controller.errors.exception;


import lombok.experimental.StandardException;

/**
 * Thrown when a requested time period is invalid or not permitted.
 */
@StandardException
public class IllegalTimePeriodException extends RuntimeException
{
}
