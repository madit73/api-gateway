package com.madi.common.utility.rest.controller.errors.exception;


import lombok.experimental.StandardException;

/**
 * Thrown when a delete operation fails due to existing references.
 */
@StandardException
public class DeleteEntityConflictException extends RuntimeException
{
}
