package com.madi.limitless.common.utility.rest.controller.errors.general;


import lombok.Builder;

@Builder
public record Failure(Throwable object, String message)
{
}
