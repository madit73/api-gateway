package com.madi.limitless.common.utility.rest.controller.errors.general;


import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Represents the result of an operation with both successful and failed outcomes.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MixedResult
{
    private List<Long> successful;
    private List<Throwable> exceptions;
}

