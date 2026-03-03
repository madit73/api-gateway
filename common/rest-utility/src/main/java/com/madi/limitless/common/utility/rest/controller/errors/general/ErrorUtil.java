package com.madi.limitless.common.utility.rest.controller.errors.general;


import java.util.Collections;
import lombok.experimental.UtilityClass;

/**
 * Factory helpers for building consistent {@link ErrorResponse} payloads.
 */
@UtilityClass
public class ErrorUtil
{
    /**
     * Creates a response with the provided explanation and recommended action.
     */
    public static ErrorResponse createErrorResponse(Exception ex, String explanation, String action)
    {

        String messageId = generateMessageId(ex);
        String message = ex.getMessage();
        String exp = explanation != null ? explanation : "An error occurred processing your request.";
        String act = action != null ? action : "Please check the request and try again.";

        ErrorDetail errorDetail = ErrorDetail.builder()
                                             .messageId(messageId)
                                             .message(message)
                                             .explanation(exp)
                                             .action(act)
                                             .build();
        return new ErrorResponse(Collections.singletonList(errorDetail));
    }

    /**
     * Creates a response using default explanation and action strings.
     */
    public static ErrorResponse createErrorResponse(Exception ex)
    {

        String messageId = generateMessageId(ex);
        String message = ex.getMessage();
        String exp = "An error occurred processing your request.";
        String act = "Please check the request and try again.";

        ErrorDetail errorDetail = ErrorDetail.builder()
                                             .messageId(messageId)
                                             .message(message)
                                             .explanation(exp)
                                             .action(act)
                                             .build();
        return new ErrorResponse(Collections.singletonList(errorDetail));
    }

    private static String generateMessageId(Exception ex)
    {
        return ex.getClass().getSimpleName();
    }
}
