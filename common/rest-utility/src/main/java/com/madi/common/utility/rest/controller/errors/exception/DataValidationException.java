package com.madi.common.utility.rest.controller.errors.exception;


import jakarta.validation.ValidationException;
import lombok.experimental.StandardException;

/**
 * Thrown when request data fails domain-specific validation rules.
 */
@StandardException
public class DataValidationException extends ValidationException
{
}
