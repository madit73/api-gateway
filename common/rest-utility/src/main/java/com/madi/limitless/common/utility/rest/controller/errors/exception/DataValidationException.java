package com.madi.limitless.common.utility.rest.controller.errors.exception;


import jakarta.validation.ValidationException;
import lombok.experimental.StandardException;

@StandardException
public class DataValidationException extends ValidationException
{
}