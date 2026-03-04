package com.madi.common.utility.rest.controller.errors.general;


import lombok.Builder;
import lombok.Data;
import org.springframework.validation.annotation.Validated;

/**
 * Detailed error payload included in API error responses.
 */
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
