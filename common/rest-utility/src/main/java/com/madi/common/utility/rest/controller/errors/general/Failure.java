package com.madi.common.utility.rest.controller.errors.general;


import lombok.Builder;

/**
 * Represents a single failed operation with its cause and message.
 */
@Builder
public record Failure(Throwable object, String message)
{
}
