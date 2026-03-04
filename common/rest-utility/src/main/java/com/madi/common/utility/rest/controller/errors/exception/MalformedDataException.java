package com.madi.common.utility.rest.controller.errors.exception;


import lombok.experimental.StandardException;

/**
 * Checked exception for malformed or unreadable input payloads.
 */
@StandardException
public class MalformedDataException extends Exception
{
}
