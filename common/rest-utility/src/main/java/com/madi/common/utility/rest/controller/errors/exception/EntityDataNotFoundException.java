package com.madi.common.utility.rest.controller.errors.exception;


import lombok.experimental.StandardException;

/**
 * Thrown when expected entity data is missing or cannot be located.
 */
@StandardException
public class EntityDataNotFoundException extends DataValidationException
{
}
