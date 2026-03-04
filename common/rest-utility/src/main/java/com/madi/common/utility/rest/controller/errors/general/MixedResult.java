package com.madi.common.utility.rest.controller.errors.general;


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
    /** Identifiers of successful operations. */
    private List<Long> successful;
    /** Exceptions from failed operations. */
    private List<Throwable> exceptions;
}
