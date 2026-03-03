package com.madi.limitless.common.utility.rest.controller.errors.general;


import java.util.Collections;
import lombok.experimental.UtilityClass;

@UtilityClass
public class ErrorUtil
{
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
