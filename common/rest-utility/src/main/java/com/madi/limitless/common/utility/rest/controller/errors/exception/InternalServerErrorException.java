package com.madi.limitless.common.utility.rest.controller.errors.exception;


import lombok.experimental.StandardException;

/**
 * Thrown to signal an unexpected server-side failure.
 */
@StandardException
public class InternalServerErrorException extends RuntimeException
{
}
