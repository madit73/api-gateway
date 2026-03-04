package com.madi.common.utility.rest.controller.errors.exception;


import lombok.experimental.StandardException;

/**
 * Thrown when a requested resource cannot be found.
 */
@StandardException
public class ResourceNotFoundException extends RuntimeException
{
}
