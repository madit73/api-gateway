package com.madi.limitless.common.utility.rest.controller.errors.general;


import lombok.Builder;
import lombok.Data;
import org.springframework.validation.annotation.Validated;

@Data
@Builder
@Validated
public class ErrorDetail
{
    private String messageId;
    private String message;
    private String explanation;
    private String action;
}
