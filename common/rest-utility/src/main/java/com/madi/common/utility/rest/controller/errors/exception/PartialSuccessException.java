package com.madi.common.utility.rest.controller.errors.exception;


import com.madi.common.utility.rest.controller.errors.general.MixedResult;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.validation.annotation.Validated;

/**
 * Signals a partial success result where some operations failed.
 */
@Data
@Validated
@AllArgsConstructor
public class PartialSuccessException extends RuntimeException
{
    @NotNull
    private MixedResult mixedResult;
}
