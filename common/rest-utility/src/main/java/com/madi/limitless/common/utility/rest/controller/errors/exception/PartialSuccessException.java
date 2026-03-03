package com.madi.limitless.common.utility.rest.controller.errors.exception;


import com.madi.limitless.common.utility.rest.controller.errors.general.MixedResult;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.validation.annotation.Validated;

@Data
@Validated
@AllArgsConstructor
public class PartialSuccessException extends RuntimeException
{
    @NotNull
    private MixedResult mixedResult;
}